
package com.paysystem.paysystem.service;

import com.paysystem.paysystem.domain.Payment;

public interface PaymentGatewayService {
    boolean callGateway(Payment payment);
}
