

package com.paysystem.paysystem.service;

import com.paysystem.paysystem.domain.*;
import com.paysystem.paysystem.dto.PaymentRequest;
import com.paysystem.paysystem.dto.PaymentResponse;
import com.paysystem.paysystem.repository.PaymentRepository;
import com.paysystem.paysystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final RedisLockService redisLockService;
    private final PaymentGatewayService paymentGatewayService; // Assume this service communicates with external PG

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String lockKey = "payment_lock:" + request.getOrderId();
        if (!redisLockService.acquireLock(lockKey, Duration.ofSeconds(10))) {
            throw new IllegalStateException("Another payment for orderId " + request.getOrderId() + " is already in progress.");
        }

        try {
            // Idempotency Check
            paymentRepository.findByOrderId(request.getOrderId()).ifPresent(p -> {
                throw new IllegalStateException("Payment for orderId " + request.getOrderId() + " already exists.");
            });

            // 1. Create and save Payment entity in REQUESTED state
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .paymentMethod(request.getPaymentMethod())
                    .status(PaymentStatus.REQUESTED)
                    .build();
            paymentRepository.save(payment);

            // 2. Call external Payment Gateway
            boolean pgSuccess = paymentGatewayService.callGateway(payment);

            // 3. Update payment status based on PG response
            if (pgSuccess) {
                payment.setStatus(PaymentStatus.SUCCESS);
                // In a real scenario, you would get a transaction ID from the PG
                payment.setPgTransactionId("PG_TXN_" + System.currentTimeMillis());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);

            // 4. Record the transaction log
            Transaction transaction = Transaction.builder()
                    .payment(payment)
                    .transactionType(TransactionType.PAYMENT)
                    .status(pgSuccess ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                    .amount(payment.getAmount())
                    .pgResponse(pgSuccess ? "{\"code\":\"SUCCESS\"}" : "{\"code\":\"FAILED\"}") // Mock PG response
                    .build();
            transactionRepository.save(transaction);

            // TODO: Publish an event to save detailed logs to MongoDB and index in Elasticsearch asynchronously

            return PaymentResponse.from(payment);

        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }
}
