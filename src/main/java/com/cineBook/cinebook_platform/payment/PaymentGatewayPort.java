package com.cineBook.cinebook_platform.payment;


import java.math.BigDecimal;

public interface PaymentGatewayPort {
    PaymentInitiateResponse initiatePayment(String bookingId, BigDecimal amount);
    PaymentConfirmResponse confirmPayment(String paymentId);
    RefundResponse initiateRefund(String paymentId, BigDecimal amount);
}

record RefundResponse(String refundId, String paymentId, BigDecimal amount, String status) {}
