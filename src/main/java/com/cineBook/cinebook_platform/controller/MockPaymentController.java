package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.payment.MockPaymentService;
import com.cineBook.cinebook_platform.payment.PaymentConfirmResponse;
import com.cineBook.cinebook_platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mock-payment")
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class MockPaymentController {

    private final MockPaymentService mockPaymentService;
    private final BookingService bookingService;

    /**
     * Simulates the user clicking "Pay Now" on the gateway's hosted page.
     *
     * In production this is replaced by a real webhook from Razorpay/Stripe
     * hitting POST /api/v1/payments/webhook with a signed payload.
     *
     * Demo usage:
     *   POST http://localhost:8080/mock-payment/confirm?paymentId=MOCK-PAY-XXXXXXXX
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestParam String paymentId) {

        // Look up which booking this payment belongs to
        MockPaymentService.PaymentRecord record =
                mockPaymentService.getPayments().get(paymentId);

        if (record == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment not found: " + paymentId));
        }

        PaymentConfirmResponse response = mockPaymentService.confirmPayment(paymentId);

        if ("SUCCESS".equals(response.status())) {
            // This is exactly what a real payment webhook does in production
            bookingService.confirmBooking(record.bookingId(), paymentId);

            return ResponseEntity.ok(Map.of(
                    "paymentId",        paymentId,
                    "bookingId",        record.bookingId(),
                    "status",           "SUCCESS",
                    "message",          "Payment confirmed. Booking is now CONFIRMED."
            ));
        }

        return ResponseEntity.status(402).body(Map.of(
                "paymentId",    paymentId,
                "status",       "FAILED",
                "reason",       response.failureReason()
        ));
    }

    /**
     * Shows all in-memory payments — useful during demo to see
     * what payments are pending confirmation.
     */
    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> listPayments() {
        var payments = mockPaymentService.getPayments();
        return ResponseEntity.ok(Map.of(
                "count",    payments.size(),
                "payments", payments.values().stream()
                        .map(p -> Map.of(
                                "paymentId", p.paymentId(),
                                "bookingId", p.bookingId(),
                                "amount",    p.amount(),
                                "status",    p.status()
                        ))
                        .toList()
        ));
    }
}