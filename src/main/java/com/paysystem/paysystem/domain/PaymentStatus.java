
package com.paysystem.paysystem.domain;

public enum PaymentStatus {
    REQUESTED, // 결제 요청됨
    SUCCESS,   // 결제 성공
    FAILED,    // 결제 실패
    CANCELLED  // 결제 취소됨
}
