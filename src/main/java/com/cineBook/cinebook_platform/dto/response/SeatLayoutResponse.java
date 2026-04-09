package com.cineBook.cinebook_platform.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response for GET /api/v1/theatre-admin/inventory/shows/{showId}
 *
 * Returns the full seat map for a show — used by:
 *   1. Theatre partners to see/manage seat status
 *   2. Customer seat selection screen (same endpoint, filtered to AVAILABLE seats)
 */
@Value
@Builder
public class SeatLayoutResponse {

    String showId;
    Integer totalSeats;
    Integer availableSeats;
    Integer bookedSeats;
    Integer blockedSeats;

    List<SeatInfo> seats;

    @Value
    @Builder
    public static class SeatInfo {
        String seatId;           // ShowSeatInventory ID — used in BookTicketRequest.seatIds
        String seatNumber;       // display label, e.g. "A1"
        String rowLabel;         // e.g. "A"
        String category;         // GENERAL, PREMIUM, RECLINER, COUPLE
        String status;           // AVAILABLE, BOOKED, LOCKED, BLOCKED
        BigDecimal price;        // final price for this seat in this show
    }
}
