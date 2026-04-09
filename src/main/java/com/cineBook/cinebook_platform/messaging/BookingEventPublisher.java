package com.cineBook.cinebook_platform.messaging;

import com.cineBook.cinebook_platform.messaging.events.BookingCancelledEvent;
import com.cineBook.cinebook_platform.messaging.events.BookingConfirmedEvent;
import com.cineBook.cinebook_platform.messaging.events.ShowCancelledEvent;

import java.math.BigDecimal;

// Port interface — both local and prod implement this
public interface BookingEventPublisher {
    void publishBookingConfirmed(BookingConfirmedEvent event);
    void publishBookingCancelled(BookingCancelledEvent event);
    void publishShowCancelled(ShowCancelledEvent event);
}
