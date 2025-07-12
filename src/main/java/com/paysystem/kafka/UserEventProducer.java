package com.paysystem.kafka;

import com.paysystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class UserEventProducer {

    private static final String TOPIC = "user-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserCreatedEvent(User user) {
        log.info("Sending user created event for user: {}", user.getUsername());
        kafkaTemplate.send(TOPIC, "User created: " + user.getUsername());
    }
}