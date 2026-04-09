package com.cineBook.cinebook_platform.service;

import com.cineBook.cinebook_platform.dto.BookingDTO;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.*;
import com.cineBook.cinebook_platform.payment.MockPaymentService;
import com.cineBook.cinebook_platform.payment.PaymentConfirmResponse;
import com.cineBook.cinebook_platform.payment.PaymentInitiateResponse;
import com.cineBook.cinebook_platform.repository.*;
import com.cineBook.cinebook_platform.service.DiscountEngine.DiscountService;
import com.cineBook.cinebook_platform.service.DiscountEngine.DiscountContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * WRITE SCENARIOS (B2C - End Customer):
 *  1. Book movie tickets (select theatre, timing, seats)
 *  2. Bulk booking (multiple shows / multiple seats)
 *  3. Cancellation (single + bulk)
 *
 * KEY DESIGN DECISIONS:
 * - Pessimistic locking on ShowSeatInventory during seat selection
 *   prevents double-booking under concurrent load.
 * - @Transactional on booking ensures seat status + booking record
 *   are updated atomically.
 * - Discount engine is invoked before persisting to compute net amount.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final ShowRepository showRepository;
    private final ShowSeatInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final DiscountService discountService;

    private final MockPaymentService mockPaymentService; // Simulates payment gateway interactions

    // Convenience fee: flat 2% of net amount
    private static final BigDecimal CONVENIENCE_FEE_RATE = new BigDecimal("0.02");

    // ─────────────────────────────────────────────────────────
    // 1. BOOK TICKETS (single booking)
    // ─────────────────────────────────────────────────────────

    @Transactional
    public BookingDTO.Response bookTickets(BookingDTO.BookRequest request, String userId) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new CineBookException("Show not found"));

        if (show.getStatus() != Show.ShowStatus.SCHEDULED) {
            throw new CineBookException("Show is no longer available for booking.");
        }

        // Step 1: Lock requested seats — prevents concurrent double booking
        List<ShowSeatInventory> lockedSeats = inventoryRepository
                .findAvailableSeatsWithLock(request.getShowId(), request.getSeatIds());

        if (lockedSeats.size() != request.getSeatIds().size()) {
            throw new CineBookException(
                "One or more selected seats are no longer available. Please reselect.");
        }

        // Step 2: Mark seats as LOCKED (short-lived state until payment confirmed)
        lockedSeats.forEach(s -> s.setSeatStatus(ShowSeatInventory.SeatStatus.LOCKED));
        inventoryRepository.saveAll(lockedSeats);

        // Step 3: Compute discounts
        List<BigDecimal> prices = lockedSeats.stream()
                .map(ShowSeatInventory::getPrice)
                .collect(Collectors.toList());

        var discountBreakdown = discountService.calculate(new DiscountContext(show, prices));

        BigDecimal grossAmount = discountBreakdown.grossAmount();
        BigDecimal discount = discountBreakdown.totalDiscount();
        BigDecimal netAmount = grossAmount.subtract(discount);
        BigDecimal convenienceFee = netAmount.multiply(CONVENIENCE_FEE_RATE)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalAmount = netAmount.add(convenienceFee);

        // Step 4: Create Booking record (PENDING until payment gateway callback)
        Booking booking = Booking.builder()
                .bookingReference(generateBookingRef())
                .show(show)
                .userId(userId)
                .bookedAt(LocalDateTime.now())
                .status(Booking.BookingStatus.PENDING)
                .grossAmount(grossAmount)
                .discountAmount(discount)
                .netAmount(netAmount)
                .convenienceFee(convenienceFee)
                .totalAmount(totalAmount)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Step 5: Create BookedSeat records
        List<BookedSeat> bookedSeats = lockedSeats.stream()
                .map(ssi -> BookedSeat.builder()
                        .booking(saved)
                        .showSeatInventory(ssi)
                        .priceAtBooking(ssi.getPrice())
                        .discountApplied(BigDecimal.ZERO) // per-seat discount tracking can be extended
                        .build())
                .collect(Collectors.toList());
        saved.setBookedSeats(bookedSeats);

        // NOTE: Seats remain LOCKED here. A separate PaymentCallbackService
        PaymentInitiateResponse pi = mockPaymentService.initiatePayment(saved.getBookingReference(), totalAmount);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String paymentId = pi.paymentId();

        if (paymentId != null && !paymentId.isEmpty()) {
            PaymentConfirmResponse pr = mockPaymentService.confirmPayment(paymentId);

            if (pr != null && "SUCCESS".equals(pr.status())) {
                confirmBooking(saved.getId(), paymentId);
            }
        }
        // A TTL job (scheduled task) will release LOCKED seats after ~10 min.

        log.info("Booking {} created for user {} — awaiting payment", saved.getBookingReference(), userId);

        return buildResponse(saved, discountBreakdown.appliedOffers());
    }

    // ─────────────────────────────────────────────────────────
    // 2. CONFIRM BOOKING (called by payment gateway callback)
    // ─────────────────────────────────────────────────────────

    @Caching(evict = {
            @CacheEvict(value = "seat-layout",          key = "#booking.show.id"),
            @CacheEvict(value = "available-seat-count",  key = "#booking.show.id")
    })
    @Transactional
    public void confirmBooking(String bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CineBookException("Booking not found"));

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(paymentId);
        booking.setPaymentStatus("PAID");

        // Mark seats as BOOKED
        List<String> ssiIds = booking.getBookedSeats().stream()
                .map(bs -> bs.getShowSeatInventory().getId())
                .collect(Collectors.toList());
        inventoryRepository.updateStatusForSeats(ssiIds, ShowSeatInventory.SeatStatus.BOOKED);

        bookingRepository.save(booking);
        log.info("Booking {} confirmed. PaymentId: {}", booking.getBookingReference(), paymentId);
    }

    // ─────────────────────────────────────────────────────────
    // 3. BULK BOOKING
    // ─────────────────────────────────────────────────────────

    @Transactional
    public List<BookingDTO.Response> bulkBookTickets(
            BookingDTO.BulkBookRequest request, String userId) {

        return request.getBookings().stream()
                .map(singleRequest -> {
                    try {
                        return bookTickets(singleRequest, userId);
                    } catch (CineBookException e) {
                        log.warn("Bulk booking partial failure for show {}: {}",
                                singleRequest.getShowId(), e.getMessage());
                        // Return a failure response stub
                        return BookingDTO.Response.builder()
                                .showId(singleRequest.getShowId())
                                .status("FAILED")
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // 4. CANCEL BOOKING (single)
    // ─────────────────────────────────────────────────────────

    @Transactional
    public BookingDTO.CancelResponse cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CineBookException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new CineBookException("Unauthorized: booking belongs to a different user.");
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new CineBookException("Booking is already cancelled.");
        }
        if (booking.getShow().getStartTime().isBefore(LocalDateTime.now())) {
            throw new CineBookException("Cannot cancel a past or ongoing show.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Release seats back to AVAILABLE
        List<String> ssiIds = booking.getBookedSeats().stream()
                .map(bs -> bs.getShowSeatInventory().getId())
                .collect(Collectors.toList());
        inventoryRepository.updateStatusForSeats(ssiIds, ShowSeatInventory.SeatStatus.AVAILABLE);

        // Trigger refund (async via event)
        // eventPublisher.publishEvent(new BookingCancelledEvent(bookingId, booking.getNetAmount()));

        return BookingDTO.CancelResponse.builder()
                .cancelledBookingIds(List.of(bookingId))
                .failedBookingIds(Collections.emptyList())
                .totalRefundAmount(booking.getNetAmount())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // 5. BULK CANCELLATION
    // ─────────────────────────────────────────────────────────

    @Transactional
    public BookingDTO.CancelResponse bulkCancelBookings(
            BookingDTO.CancelRequest request, String userId) {

        List<Booking> bookings = bookingRepository
                .findByIdInAndUserId(request.getBookingIds(), userId);

        List<String> cancelled = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (Booking booking : bookings) {
            try {
                BookingDTO.CancelResponse r = cancelBooking(booking.getId(), userId);
                cancelled.addAll(r.getCancelledBookingIds());
                totalRefund = totalRefund.add(r.getTotalRefundAmount());
            } catch (CineBookException e) {
                failed.add(booking.getId());
                log.warn("Bulk cancel failed for booking {}: {}", booking.getId(), e.getMessage());
            }
        }

        return BookingDTO.CancelResponse.builder()
                .cancelledBookingIds(cancelled)
                .failedBookingIds(failed)
                .totalRefundAmount(totalRefund)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private static final AtomicInteger BOOKING_COUNTER = new AtomicInteger(1000);

    private String generateBookingRef() {
        return "CB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);  // In production: use a distributed ID generator (Snowflake / DB sequence)
    }

    private BookingDTO.Response buildResponse(
            Booking booking, List<BookingDTO.AppliedOffer> offers) {

        List<String> seatNumbers = booking.getBookedSeats().stream()
                .map(bs -> bs.getShowSeatInventory().getSeat().getSeatNumber())
                .collect(Collectors.toList());

        return BookingDTO.Response.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .showId(booking.getShow().getId())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .theatreName(booking.getShow().getTheatre().getName())
                .showStartTime(booking.getShow().getStartTime())
                .seatNumbers(seatNumbers)
                .grossAmount(booking.getGrossAmount())
                .discountAmount(booking.getDiscountAmount())
                .netAmount(booking.getNetAmount())
                .convenienceFee(booking.getConvenienceFee())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .paymentId(booking.getPaymentId())
                .appliedOffers(offers)
                .build();
    }
}
