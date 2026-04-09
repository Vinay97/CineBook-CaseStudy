package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request body for POST /api/v1/bookings
 * Used by B2C customers to book seats for a show.
 */
@Data
public class BookTicketRequest {

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Cannot book more than 10 seats in a single transaction")
    private List<String> seatIds;  // ShowSeatInventory IDs

    // Optional — applied after discount engine runs
    private String promoCode;
}
