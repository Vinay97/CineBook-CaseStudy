package com.cineBook.cinebook_platform.messaging.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent {

    private String eventId;           // unique event ID for idempotency
    private String bookingId;
    private String bookingReference;  // e.g. CB-2024-001234
    private String userId;
    private String showId;
    private String movieTitle;
    private String theatreName;
    private String city;
    private LocalDateTime showStartTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private LocalDateTime occurredAt;
}
