package com.cineBook.cinebook_platform.messaging.mock;


import com.cineBook.cinebook_platform.messaging.events.BookingCancelledEvent;
import com.cineBook.cinebook_platform.messaging.events.BookingConfirmedEvent;
import com.cineBook.cinebook_platform.messaging.events.ShowCancelledEvent;
import com.cineBook.cinebook_platform.messaging.BookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalSyncBookingEventPublisher implements BookingEventPublisher {

    // In local mode we call downstream logic directly and synchronously.
    // No broker involved — but the same event object is used, so the
    // contract between producer and consumer is still exercised.

    @Override
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("[LocalKafkaMock] BookingConfirmed — bookingId: {}, user: {}, movie: {}",
                event.getBookingId(), event.getUserId(), event.getMovieTitle());
        // Wire in NotificationService here if you want emails/SMS in local demo
    }

    @Override
    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("[LocalKafkaMock] BookingCancelled — bookingId: {}, refund: {}",
                event.getBookingId(), event.getRefundAmount());
    }

    @Override
    public void publishShowCancelled(ShowCancelledEvent event) {
        log.info("[LocalKafkaMock] ShowCancelled — showId: {}, movie: {}, date: {}",
                event.getShowId(), event.getMovieTitle(), event.getShowDate());
    }
}
