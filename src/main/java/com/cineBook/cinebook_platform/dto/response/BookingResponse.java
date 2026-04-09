package com.cineBook.cinebook_platform.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for:
 *   POST /api/v1/bookings          (single booking)
 *   POST /api/v1/bookings/bulk     (each item in the list)
 *
 * Returned immediately after the booking is created with PENDING status.
 * Status changes to CONFIRMED after payment gateway callback.
 */
@Value
@Builder
public class BookingResponse {

    String bookingId;
    String bookingReference;     // human-readable, e.g. CB-2024-001234

    // Show details — denormalised so the client doesn't need a second call
    String showId;
    String movieTitle;
    String theatreName;
    String city;
    String screenName;
    LocalDateTime showStartTime;

    // Seat details
    List<String> seatNumbers;    // e.g. ["A1", "A2", "A3"]

    // Financials
    BigDecimal grossAmount;      // sum of seat prices before discount
    BigDecimal discountAmount;   // total discount applied
    BigDecimal netAmount;        // grossAmount − discountAmount
    BigDecimal convenienceFee;   // platform fee (2% of netAmount)
    BigDecimal totalAmount;      // netAmount + convenienceFee

    // Booking status
    String status;               // PENDING → CONFIRMED after payment

    String paymentId;            // populated after payment gateway confirms

    // Which offers were applied and how much each saved
    List<AppliedOfferDetail> appliedOffers;

    @Value
    @Builder
    public static class AppliedOfferDetail {
        String offerCode;
        String description;
        BigDecimal discountAmount;
    }
}
