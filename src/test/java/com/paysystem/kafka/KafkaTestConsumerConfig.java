package com.paysystem.kafka;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@TestConfiguration
public class KafkaTestConsumerConfig {
    // Bean 정의를 모두 제거합니다.
    // 이 클래스는 테스트 중에 @KafkaListener 어노테이션을 활성화하는 역할만 수행합니다.
    // 실제 Bean 생성은 Spring Boot의 자동 구성에 맡깁니다.
}