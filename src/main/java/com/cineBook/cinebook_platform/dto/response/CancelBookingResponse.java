package com.cineBook.cinebook_platform.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response for:
 *   DELETE /api/v1/bookings/{bookingId}   (single cancel)
 *   POST   /api/v1/bookings/cancel-bulk   (bulk cancel)
 *
 * Partial success is supported — some bookings may fail (e.g. show already started).
 * The client receives both succeeded and failed lists.
 */
@Value
@Builder
public class CancelBookingResponse {

    List<String> cancelledBookingIds;
    List<String> failedBookingIds;
    BigDecimal totalRefundAmount;     // total across all successfully cancelled bookings
}
