package com.cineBook.cinebook_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ─────────────────────────────────────────────────────────────
// THEATRE DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// SHOW DTOs
// ─────────────────────────────────────────────────────────────

public class ShowDTO {

    /** Request body for creating a show (B2B - Theatre Partner) */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        private String movieId;
        private String theatreId;
        private String screenId;
        private LocalDate showDate;
        private LocalDateTime startTime;
        private BigDecimal basePrice;
    }

    /** Request body for updating a show */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateRequest {
        private LocalDateTime startTime;
        private BigDecimal basePrice;
        private String status; // SCHEDULED, CANCELLED
    }

    /** Response for show details */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private String showId;
        private String movieTitle;
        private String movieLanguage;
        private String theatreName;
        private String city;
        private String screenName;
        private String screenType;
        private LocalDate showDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String slot;          // MORNING, AFTERNOON, EVENING, NIGHT
        private BigDecimal basePrice;
        private Integer totalSeats;
        private Integer availableSeats;
        private String status;
        private List<OfferInfo> applicableOffers;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OfferInfo {
        private String offerCode;
        private String description;
        private String discountType; // PERCENTAGE, FLAT
        private BigDecimal discountValue;
    }
}

// ─────────────────────────────────────────────────────────────
// SEAT INVENTORY DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// BOOKING DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// BROWSE / SEARCH DTOs
// ─────────────────────────────────────────────────────────────

