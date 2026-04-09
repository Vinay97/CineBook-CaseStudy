package com.cineBook.cinebook_platform.controller;
import com.cineBook.cinebook_platform.dto.BookingDTO;
import com.cineBook.cinebook_platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// ─────────────────────────────────────────────────────────────
// B2C: Booking Controller
// ─────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/v1/bookings
     * Body: { showId, seatIds: [...], promoCode }
     *
     * Book seats for a show
     */
    @PostMapping
    public ResponseEntity<BookingDTO.Response> bookTickets(
            @RequestBody BookingDTO.BookRequest request,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(bookingService.bookTickets(request, userId));
    }

    /**
     * POST /api/v1/bookings/bulk
     * Body: { bookings: [{showId, seatIds}, ...] }
     *
     * Bulk booking — multiple shows
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<BookingDTO.Response>> bulkBookTickets(
            @RequestBody BookingDTO.BulkBookRequest request,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(bookingService.bulkBookTickets(request, userId));
    }

    /**
     * DELETE /api/v1/bookings/{bookingId}
     *
     * Cancel a single booking
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<BookingDTO.CancelResponse> cancelBooking(
            @PathVariable String bookingId,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, userId));
    }

    /**
     * POST /api/v1/bookings/cancel-bulk
     * Body: { bookingIds: [...], reason }
     *
     * Bulk cancellation
     */
    @PostMapping("/cancel-bulk")
    public ResponseEntity<BookingDTO.CancelResponse> bulkCancelBookings(
            @RequestBody BookingDTO.CancelRequest request,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(bookingService.bulkCancelBookings(request, userId));
    }
}
