
package com.paysystem.paysystem.service;

import com.paysystem.paysystem.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// This is a mock implementation for demonstration purposes.
// In a real-world scenario, this service would make an HTTP call to an external Payment Gateway.
@Slf4j
@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public boolean callGateway(Payment payment) {
        log.info("Calling external PG for orderId: {}", payment.getOrderId());
        // Simulate network latency
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        // Simulate a successful payment for amounts not ending in 0.99
        boolean success = !payment.getAmount().toPlainString().endsWith(".99");

        if (success) {
            log.info("PG call successful for orderId: {}", payment.getOrderId());
        } else {
            log.error("PG call failed for orderId: {}", payment.getOrderId());
        }
        return success;
    }
}
