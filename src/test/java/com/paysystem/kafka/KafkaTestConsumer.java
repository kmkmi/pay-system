package com.paysystem.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KafkaTestConsumer {

    @Getter
    private String payload;
    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "user-events", groupId = "test-group")
    public void receive(String payload) {
        log.info("Received payload='{}'", payload);
        this.payload = payload;
        latch.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }

    /**
     * 각 테스트 실행 전에 상태를 초기화하여 테스트 격리성을 보장합니다.
     */
    public void reset() {
        latch = new CountDownLatch(1);
        this.payload = null;
    }
}