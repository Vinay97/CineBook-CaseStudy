package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request body for POST /api/v1/theatre-admin/shows
 * Used by B2B theatre partners to create a new show.
 */
@Data
public class CreateShowRequest {

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    @NotBlank(message = "Theatre ID is required")
    private String theatreId;

    @NotBlank(message = "Screen ID is required")
    private String screenId;

    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date cannot be in the past")
    private LocalDate showDate;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero")
    private BigDecimal basePrice;
}
