
package com.paysystem.paysystem.dto;

import com.paysystem.paysystem.domain.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String orderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String pgTransactionId;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .pgTransactionId(payment.getPgTransactionId())
                .build();
    }
}
