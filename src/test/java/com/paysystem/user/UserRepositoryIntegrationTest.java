package com.paysystem.user;

import com.paysystem.kafka.KafkaTestConsumer;
import com.paysystem.kafka.KafkaTestConsumerConfig;
import com.paysystem.kafka.UserEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h3>전체 통합 테스트</h3>
 * Testcontainers를 사용하여 실제 인프라(MySQL, Redis, Kafka)와
 * 동일한 환경에서 통합 테스트를 수행합니다.
 */
@Testcontainers // 1. JUnit 5에게 Testcontainers를 사용함을 알립니다.
@SpringBootTest   // 2. 실제 애플리케이션과 동일한 Spring Context를 로드합니다.
@ActiveProfiles("test") // 3. 'application-test.properties' 설정을 활성화합니다.
@Import(KafkaTestConsumerConfig.class) // 3-1. 테스트용 Kafka Consumer 설정을 명시적으로 불러옵니다.
class UserRepositoryIntegrationTest {

    // 4. 모든 테스트 메서드가 공유할 MySQL 컨테이너를 static으로 선언합니다.
    // JUnit 5가 테스트 시작 전 컨테이너를 실행하고, 테스트 종료 후 자동으로 중지합니다.
    @Container
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    // 4-1. Redis 컨테이너를 추가로 선언합니다.
    // GenericContainer를 사용하면 특정 명령어(--requirepass)를 전달하기 용이합니다.
    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379) // Redis 기본 포트를 외부에 노출합니다.
            .withCommand("redis-server", "--requirepass", "password"); // .env.example과 동일한 비밀번호를 설정합니다.
    


    // 5. Spring Context가 로드되기 전에 컨테이너의 동적 속성(포트, 주소 등)을 주입합니다.
    // 이를 통해 Spring Boot는 실제 DB가 아닌 테스트 컨테이너에 연결됩니다.
    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // MySQL 컨테이너 속성 주입
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);

        // Redis 컨테이너 속성 주입
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.data.redis.password", () -> "password");


    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserEventProducer userEventProducer;
    @Autowired
    private KafkaTestConsumer kafkaTestConsumer;

    @BeforeEach
    void setUp() {
        kafkaTestConsumer.reset();
    }

    @Test
    @DisplayName("사용자를 저장하고 ID로 조회하면 정상적으로 조회되어야 한다.")
    void saveAndFindUser_shouldSucceed() {
        // given: 테스트할 User 객체를 생성합니다.
        User newUser = new User("test-user");

        // when: UserRepository를 통해 User를 저장하고 다시 조회합니다.
        User savedUser = userRepository.save(newUser);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        // then: 조회된 User가 null이 아니며, 저장된 값과 일치하는지 검증합니다.
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("사용자를 저장하고 Redis에 캐시하면 두 곳 모두에서 정상적으로 조회되어야 한다.")
    void saveUserAndCacheInRedis_shouldSucceed() {
        // given: 테스트할 User 객체와 Redis에 사용할 Key, Value를 준비합니다.
        User newUser = new User("cached-user");

        // when: DB에 사용자를 저장합니다.
        User savedUser = userRepository.save(newUser);
        String redisKey = "user:" + savedUser.getId();

        // and when: Redis에 사용자 이름을 캐시합니다.
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(redisKey, savedUser.getUsername());

        // then: DB에서 사용자가 올바르게 조회되는지 확인합니다.
        User foundUserInDb = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(foundUserInDb).isNotNull();
        assertThat(foundUserInDb.getUsername()).isEqualTo("cached-user");

        // and then: Redis에서 사용자 이름이 올바르게 조회되는지 확인합니다.
        String foundUsernameInRedis = ops.get(redisKey);
        assertThat(foundUsernameInRedis).isEqualTo("cached-user");
    }

    @Test
    @DisplayName("사용자를 생성하면 Kafka로 이벤트가 발행되고, Consumer가 이를 수신해야 한다.")
    void whenUserIsCreated_eventShouldBePublishedToKafka() throws InterruptedException {
        // given: 테스트할 User 객체를 생성합니다.
        User newUser = new User("kafka-user");

        // when: DB에 사용자를 저장하고 Kafka 이벤트를 발행합니다.
        User savedUser = userRepository.save(newUser);
        userEventProducer.sendUserCreatedEvent(savedUser);

        // then: Kafka Consumer가 메시지를 수신할 때까지 최대 10초 대기합니다.
        boolean messageReceived = kafkaTestConsumer.await(10, TimeUnit.SECONDS);

        // and then: 메시지 수신 성공 여부와 내용을 검증합니다.
        assertThat(messageReceived).isTrue();
        assertThat(kafkaTestConsumer.getPayload()).isEqualTo("User created: kafka-user");
    }
}