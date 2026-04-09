package com.cineBook.cinebook_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

public class SeatInventoryDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AllocateRequest {
        private String showId;
        private List<SeatAllocation> seats;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SeatAllocation {
        private String seatId;
        private String status;   // AVAILABLE, BLOCKED
        private BigDecimal price;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SeatLayoutResponse {
        private String showId;
        private List<SeatInfo> seats;
        private Integer totalSeats;
        private Integer availableSeats;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SeatInfo {
        private String seatId;
        private String seatNumber;
        private String rowLabel;
        private String category;   // GENERAL, PREMIUM, RECLINER, COUPLE
        private String status;     // AVAILABLE, BOOKED, LOCKED, BLOCKED
        private BigDecimal price;
    }
}

// ─────────────────────────────────────────────────────────────
// BOOKING DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// BROWSE / SEARCH DTOs
// ─────────────────────────────────────────────────────────────

