package com.cineBook.cinebook_platform.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("local")   // only active when running locally
@Slf4j
public class MockPaymentService implements PaymentGatewayPort {

    // Simulates the gateway's payment records in memory
    private final Map<String, PaymentRecord> payments = new ConcurrentHashMap<>();
    public Map<String, PaymentRecord> getPayments() {
        return Collections.unmodifiableMap(payments);
    }

    @Override
    public PaymentInitiateResponse initiatePayment(String bookingId, BigDecimal amount) {
        String paymentId = "MOCK-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        payments.put(paymentId, new PaymentRecord(
                paymentId, bookingId, amount, "PENDING", LocalDateTime.now()
        ));

        log.info("[MockPayment] Payment initiated — id: {}, bookingId: {}, amount: {}",
                paymentId, bookingId, amount);

        // Simulate the gateway returning a redirect URL
        return new PaymentInitiateResponse(
                paymentId,
                "http://localhost:8080/mock-payment?paymentId=" + paymentId,
                "PENDING"
        );
    }

    @Override
    public PaymentConfirmResponse confirmPayment(String paymentId) {
        PaymentRecord record = payments.get(paymentId);
        if (record == null) {
            return new PaymentConfirmResponse(paymentId, "FAILED", "Payment not found");
        }

        // Simulate 10% failure rate based on amount ending in 0
        // (easy to trigger in tests by using amount like 250, 500 etc.)
        boolean simulateFailure = record.amount().remainder(BigDecimal.TEN).compareTo(BigDecimal.ZERO) == 0
                && record.amount().compareTo(new BigDecimal("1000")) > 0;

        String status = simulateFailure ? "FAILED" : "SUCCESS";
        payments.put(paymentId, record.withStatus(status));

        log.info("[MockPayment] Payment {} — id: {}", status, paymentId);
        return new PaymentConfirmResponse(paymentId, status, simulateFailure ? "Simulated failure" : null);
    }

    @Override
    public RefundResponse initiateRefund(String paymentId, BigDecimal amount) {
        String refundId = "MOCK-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MockPayment] Refund initiated — refundId: {}, paymentId: {}, amount: {}",
                refundId, paymentId, amount);
        return new RefundResponse(refundId, paymentId, amount, "REFUND_INITIATED");
    }

    // ── Record types ──────────────────────────────────────────

    public record PaymentRecord(
            String paymentId, String bookingId,
            BigDecimal amount, String status, LocalDateTime createdAt) {

        public PaymentRecord withStatus(String newStatus) {
            return new PaymentRecord(paymentId, bookingId, amount, newStatus, createdAt);
        }
    }
}
