package com.cineBook.cinebook_platform.messaging.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {

    private String eventId;
    private String bookingId;
    private String bookingReference;
    private String userId;
    private String showId;
    private String paymentId;          // needed by RefundConsumer to trigger refund
    private BigDecimal refundAmount;
    private String cancellationReason;
    private LocalDateTime occurredAt;
}
