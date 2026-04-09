package com.cineBook.cinebook_platform.payment;

public record PaymentInitiateResponse(String paymentId, String redirectUrl, String status) {}
