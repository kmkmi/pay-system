
package com.paysystem.paysystem.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "payment_transactions")
@Getter
@Builder
public class PaymentTransactionDocument {

    @Id
    private String id;

    @Field("payment_id")
    private Long paymentId;

    @Field("order_id")
    private String orderId;

    @Field("transaction_type")
    private String transactionType;

    @Field("status")
    private String status;

    @Field("pg_request")
    private Object pgRequest;

    @Field("pg_response")
    private Object pgResponse;

    @Field("created_at")
    private LocalDateTime createdAt;
}
