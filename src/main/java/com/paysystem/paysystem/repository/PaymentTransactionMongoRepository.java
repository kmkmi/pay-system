
package com.paysystem.paysystem.repository;

import com.paysystem.paysystem.domain.PaymentTransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentTransactionMongoRepository extends MongoRepository<PaymentTransactionDocument, String> {
}
