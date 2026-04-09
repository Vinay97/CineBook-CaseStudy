package com.cineBook.cinebook_platform.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for a single show — used in:
 *   POST   /api/v1/theatre-admin/shows        (create)
 *   PUT    /api/v1/theatre-admin/shows/{id}   (update)
 *   GET    /api/v1/browse/movies/{id}/theatres (embedded inside TheatreWithShowsResponse)
 */
@Value
@Builder
public class ShowResponse {

    String showId;

    // Movie info
    String movieTitle;
    String movieLanguage;
    String genre;

    // Venue info
    String theatreId;
    String theatreName;
    String city;
    String screenName;
    String screenType;          // STANDARD, IMAX, DOLBY, FOUR_DX

    // Timing
    LocalDate showDate;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String slot;                // MORNING, AFTERNOON, EVENING, NIGHT

    // Pricing & availability
    BigDecimal basePrice;
    Integer totalSeats;
    Integer availableSeats;

    // Status
    String status;              // SCHEDULED, CANCELLED, COMPLETED

    // Offers applicable to this show — shown on browse screen
    List<OfferDetail> applicableOffers;

    @Value
    @Builder
    public static class OfferDetail {
        String offerCode;
        String description;
        String discountType;    // PERCENTAGE or FLAT
        BigDecimal discountValue;
    }
}
