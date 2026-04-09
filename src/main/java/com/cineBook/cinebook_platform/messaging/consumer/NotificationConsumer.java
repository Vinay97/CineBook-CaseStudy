package com.cineBook.cinebook_platform.messaging.consumer;

import com.cineBook.cinebook_platform.messaging.events.BookingConfirmedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")  // without this, app fails to start locally — no broker available
public class NotificationConsumer {

    @KafkaListener(topics = "booking.confirmed", groupId = "notification-group")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        // send email, SMS, push notification
    }
}
