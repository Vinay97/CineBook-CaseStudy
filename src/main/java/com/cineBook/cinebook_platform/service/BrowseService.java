package com.cineBook.cinebook_platform.service;

import com.cineBook.cinebook_platform.config.CacheNames;
import com.cineBook.cinebook_platform.dto.*;
import com.cineBook.cinebook_platform.model.Movie;
import com.cineBook.cinebook_platform.model.Show;
import com.cineBook.cinebook_platform.model.ShowSeatInventory;
import com.cineBook.cinebook_platform.repository.ShowRepository;
import com.cineBook.cinebook_platform.repository.ShowSeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * READ SCENARIO 1:
 * Browse theatres currently running a show (movie selected) in a city,
 * including show timings by a chosen date.
 *
 * READ SCENARIO 2 (Offers):
 * Booking platform offers in selected cities and theatres.
 */
@Service
@RequiredArgsConstructor
public class BrowseService {

    private final ShowRepository showRepository;
    private final ShowSeatInventoryRepository inventoryRepository;

    /**
     * Returns theatres grouped with their shows for a given movie/city/date.
     * Includes applicable offers per show.
     */
    @Cacheable(
            value = "browse-theatres",
            key = "#city + ':' + #movieId + ':' + #date + ':' + (#language ?: 'ALL')"
    )
    @Transactional(readOnly = true)
    public List<BrowseDTO.TheatreWithShows> browseTheatresForMovie(
            String movieId, String city, LocalDate date, String language) {

        List<Show> shows = (language != null && !language.isBlank())
                ? showRepository.findShowsByMovieCityDateAndLanguage(movieId, city, date, language)
                : showRepository.findShowsByMovieCityAndDate(movieId, city, date);

        // Group by theatre
        Map<String, List<Show>> byTheatre = shows.stream()
                .collect(Collectors.groupingBy(s -> s.getTheatre().getId()));

        return byTheatre.values().stream()
                .map(theatreShows -> {
                    var theatre = theatreShows.get(0).getTheatre();
                    List<ShowDTO.Response> showResponses = theatreShows.stream()
                            .map(show -> mapToShowResponse(show, computeOffers(show)))
                            .collect(Collectors.toList());

                    return BrowseDTO.TheatreWithShows.builder()
                            .theatreId(theatre.getId())
                            .theatreName(theatre.getName())
                            .address(theatre.getAddress())
                            .city(theatre.getCity())
                            .shows(showResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns all offers applicable to shows in a city/theatre — the "offers" browse screen.
     */
    @Cacheable(
            value = CacheNames.SHOW_OFFERS,
            key = "#city + ':' + #date"
    )
    @Transactional(readOnly = true)
    public List<ShowDTO.OfferInfo> getOffersForCity(String city, LocalDate date) {
        // Collect unique offers across shows in the city today
        List<Show> shows = showRepository.findShowsByMovieCityAndDate("", city, date);

        Set<String> seenOfferCodes = new HashSet<>();
        List<ShowDTO.OfferInfo> offers = new ArrayList<>();

        for (Show show : shows) {
            for (ShowDTO.OfferInfo offer : computeOffers(show)) {
                if (seenOfferCodes.add(offer.getOfferCode())) {
                    offers.add(offer);
                }
            }
        }
        return offers;
    }

    public List<BrowseDTO.MovieInfo> browseMovies(String city, LocalDate date, String status) {
        List<Show> shows = (status != null && !status.isBlank())
                ? showRepository.findShowsByCityAndDateAndStatus(city, date, Show.ShowStatus.valueOf(status))
                : showRepository.findShowsByCityAndDateAndStatus(city, date, Show.ShowStatus.SCHEDULED);

        Map<String, BrowseDTO.MovieInfo> movieMap = new HashMap<>();

        for (Show show : shows) {
            Movie movie = show.getMovie();
            movieMap.putIfAbsent(movie.getId(), BrowseDTO.MovieInfo.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .language(movie.getLanguage())
                    .genre(movie.getGenre())
                    .durationMinutes(movie.getDurationMinutes())
                    .releaseDate(movie.getReleaseDate())
                    .posterUrl(movie.getPosterUrl())
                    .rating(movie.getRating())
                    .status(movie.getStatus().name())
                    .build());
        }
        return new ArrayList<>(movieMap.values());
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private ShowDTO.Response mapToShowResponse(Show show, List<ShowDTO.OfferInfo> offers) {
        int availableSeats = inventoryRepository.countAvailableSeats(show.getId());

        return ShowDTO.Response.builder()
                .showId(show.getId())
                .movieTitle(show.getMovie().getTitle())
                .movieLanguage(show.getMovie().getLanguage())
                .theatreName(show.getTheatre().getName())
                .city(show.getTheatre().getCity())
                .screenName(show.getScreen().getName())
                .screenType(show.getScreen().getScreenType().name())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .slot(show.getSlot().name())
                .basePrice(show.getBasePrice())
                .totalSeats(show.getScreen().getTotalSeats())
                .availableSeats(availableSeats)
                .status(show.getStatus().name())
                .applicableOffers(offers)
                .build();
    }

    /**
     * Computes which platform offers apply to a given show, for display purposes.
     */
    private List<ShowDTO.OfferInfo> computeOffers(Show show) {
        List<ShowDTO.OfferInfo> offers = new ArrayList<>();

        // Offer 1: 50% off on 3rd ticket — always applicable when booking 3+
        offers.add(ShowDTO.OfferInfo.builder()
                .offerCode("THIRD_TICKET_50")
                .description("50% off on every 3rd ticket!")
                .discountType("PERCENTAGE")
                .discountValue(new java.math.BigDecimal("50"))
                .build());

        // Offer 2: 20% off for afternoon shows
        if (show.getSlot() == Show.ShowSlot.AFTERNOON) {
            offers.add(ShowDTO.OfferInfo.builder()
                    .offerCode("AFTERNOON_20")
                    .description("20% off — Afternoon Delight!")
                    .discountType("PERCENTAGE")
                    .discountValue(new java.math.BigDecimal("20"))
                    .build());
        }

        return offers;
    }
}
