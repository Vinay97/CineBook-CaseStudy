package com.cineBook.cinebook_platform.messaging.producer;


import com.cineBook.cinebook_platform.messaging.events.BookingCancelledEvent;
import com.cineBook.cinebook_platform.messaging.events.BookingConfirmedEvent;
import com.cineBook.cinebook_platform.messaging.events.ShowCancelledEvent;
import com.cineBook.cinebook_platform.messaging.BookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Profile("!local")
@RequiredArgsConstructor
@Slf4j
public class KafkaBookingEventPublisher implements BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("[Kafka] Publishing BookingConfirmedEvent — bookingId: {}",
                event.getBookingId());
        kafkaTemplate.send("booking.confirmed", event.getBookingId(), event);
    }

    @Override
    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("[Kafka] Publishing BookingCancelledEvent — bookingId: {}",
                event.getBookingId());
        kafkaTemplate.send("booking.cancelled", event.getBookingId(), event);
    }

    @Override
    public void publishShowCancelled(ShowCancelledEvent event) {
        log.info("[Kafka] Publishing ShowCancelledEvent — showId: {}",
                event.getShowId());
        kafkaTemplate.send("show.cancelled", event.getShowId(), event);
    }
}