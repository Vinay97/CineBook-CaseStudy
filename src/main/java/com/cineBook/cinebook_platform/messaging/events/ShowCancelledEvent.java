package com.cineBook.cinebook_platform.messaging.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowCancelledEvent {

    private String eventId;
    private String showId;
    private String movieTitle;
    private String theatreName;
    private String city;
    private LocalDate showDate;
    private LocalDateTime showStartTime;
    private LocalDateTime occurredAt;
    // downstream RefundConsumer will look up all confirmed
    // bookings for this showId and trigger individual refunds
}
