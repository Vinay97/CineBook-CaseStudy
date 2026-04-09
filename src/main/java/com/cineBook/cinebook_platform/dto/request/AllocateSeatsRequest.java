package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for POST /api/v1/theatre-admin/inventory/allocate
 * Theatre partners use this to set per-seat status and pricing for a show.
 */
@Data
public class AllocateSeatsRequest {

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotEmpty(message = "At least one seat allocation is required")
    @Valid
    private List<SeatAllocation> seats;

    @Data
    public static class SeatAllocation {

        @NotBlank(message = "Seat ID is required")
        private String seatId;

        /**
         * Allowed values: AVAILABLE, BLOCKED
         * BOOKED is set only by the booking flow — not manually.
         */
        @NotNull(message = "Seat status is required")
        private String status;

        /**
         * Optional override price for this seat.
         * If null, the show's base price × category multiplier is used.
         */
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        private BigDecimal price;
    }
}
