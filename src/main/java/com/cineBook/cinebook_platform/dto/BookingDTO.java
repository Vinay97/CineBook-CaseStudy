package com.cineBook.cinebook_platform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ─────────────────────────────────────────────────────────────
// THEATRE DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// SHOW DTOs
// ─────────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────────
// SEAT INVENTORY DTOs
// ─────────────────────────────────────────────────────────────



// ─────────────────────────────────────────────────────────────
// BOOKING DTOs
// ─────────────────────────────────────────────────────────────

public class BookingDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @Getter
    public static class BookRequest {
        private String showId;
        private List<String> seatIds;   // ShowSeatInventory IDs
        private String promoCode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BulkBookRequest {
        private List<BookRequest> bookings; // each can be a different show
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private String bookingId;
        private String bookingReference;
        private String showId;
        private String movieTitle;
        private String theatreName;
        private LocalDateTime showStartTime;
        private List<String> seatNumbers;
        private BigDecimal grossAmount;
        private BigDecimal discountAmount;
        private BigDecimal netAmount;
        private BigDecimal convenienceFee;
        private BigDecimal totalAmount;
        private String status;
        private String paymentId;
        private List<AppliedOffer> appliedOffers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AppliedOffer {
        private String offerCode;
        private String description;
        private BigDecimal discountAmount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CancelRequest {
        private List<String> bookingIds;
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CancelResponse {
        private List<String> cancelledBookingIds;
        private List<String> failedBookingIds;
        private BigDecimal totalRefundAmount;
    }
}

// ─────────────────────────────────────────────────────────────
// BROWSE / SEARCH DTOs
// ─────────────────────────────────────────────────────────────


