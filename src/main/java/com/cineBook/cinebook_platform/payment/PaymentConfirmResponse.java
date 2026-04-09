package com.cineBook.cinebook_platform.payment;

public record PaymentConfirmResponse(String paymentId, String status, String failureReason) {
}
