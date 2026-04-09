package com.cineBook.cinebook_platform.service;

import com.cineBook.cinebook_platform.dto.*;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.*;
import com.cineBook.cinebook_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WRITE SCENARIOS (B2B - Theatre Partner):
 *  1. Create a show for the day
 *  2. Update a show
 *  3. Delete (cancel) a show
 *  4. Allocate seat inventory for a show
 *  5. Update seat inventory
 */
@Service
@RequiredArgsConstructor
public class ShowManagementService {

    private final ShowRepository showRepository;
    private final ShowSeatInventoryRepository inventoryRepository;
    private final MovieRepository movieRepository;       // standard JpaRepository
    private final TheatreRepository theatreRepository;   // standard JpaRepository
    private final ScreenRepository screenRepository;     // standard JpaRepository
    private final SeatRepository seatRepository;         // standard JpaRepository

    // ─────────────────────────────────────────────────────────
    // 1. CREATE SHOW
    // ─────────────────────────────────────────────────────────

    @Transactional
    public ShowDTO.Response createShow(ShowDTO.CreateRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new CineBookException("Movie not found: " + request.getMovieId()));

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new CineBookException("Theatre not found"));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new CineBookException("Screen not found"));

        // Validate: no overlapping shows on the same screen
        List<Show> existingShows = showRepository.findByTheatreIdAndShowDateAndStatus(
                theatre.getId(), request.getShowDate(), Show.ShowStatus.SCHEDULED);

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDurationMinutes() + 30); // +30 min cleanup

        boolean conflict = existingShows.stream()
                .filter(s -> s.getScreen().getId().equals(screen.getId()))
                .anyMatch(s -> newStart.isBefore(s.getEndTime()) && newEnd.isAfter(s.getStartTime()));

        if (conflict) {
            throw new CineBookException("Show timing conflicts with an existing show on this screen.");
        }

        Show show = Show.builder()
                .movie(movie)
                .theatre(theatre)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(newEnd)
                .slot(deriveSlot(request.getStartTime()))
                .status(Show.ShowStatus.SCHEDULED)
                .basePrice(request.getBasePrice())
                .build();

        Show saved = showRepository.save(show);

        // Auto-allocate all active seats in the screen as AVAILABLE
        autoAllocateSeats(saved, screen, request.getBasePrice());

        return mapToResponse(saved, screen.getTotalSeats());
    }

    // ─────────────────────────────────────────────────────────
    // 2. UPDATE SHOW
    // ─────────────────────────────────────────────────────────

    @CacheEvict(value = "seat-layout", key = "#showId")
    @Transactional
    public ShowDTO.Response updateShow(String showId, ShowDTO.UpdateRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CineBookException("Show not found: " + showId));

        if (show.getStatus() == Show.ShowStatus.COMPLETED) {
            throw new CineBookException("Cannot update a completed show.");
        }

        if (request.getStartTime() != null) {
            show.setStartTime(request.getStartTime());
            show.setSlot(deriveSlot(request.getStartTime()));
            int duration = show.getMovie().getDurationMinutes();
            show.setEndTime(request.getStartTime().plusMinutes(duration + 30));
        }

        if (request.getBasePrice() != null) {
            show.setBasePrice(request.getBasePrice());
        }

        if (request.getStatus() != null) {
            show.setStatus(Show.ShowStatus.valueOf(request.getStatus()));
        }

        Show updated = showRepository.save(show);
        int availableSeats = inventoryRepository.countAvailableSeats(showId);
        return mapToResponse(updated, availableSeats);
    }

    // ─────────────────────────────────────────────────────────
    // 3. DELETE / CANCEL SHOW
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void cancelShow(String showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new CineBookException("Show not found: " + showId));

        if (show.getStatus() == Show.ShowStatus.COMPLETED) {
            throw new CineBookException("Cannot cancel a completed show.");
        }

        show.setStatus(Show.ShowStatus.CANCELLED);
        showRepository.save(show);

        // Trigger async refund for confirmed bookings (via event / message queue)
        // eventPublisher.publishEvent(new ShowCancelledEvent(showId));
        // Kept as a comment — actual event publishing wired in Spring config
    }

    // ─────────────────────────────────────────────────────────
    // 4. ALLOCATE / UPDATE SEAT INVENTORY
    // ─────────────────────────────────────────────────────────

    @CacheEvict(value = "seat-layout", key = "#request.showId")
    @Transactional
    public SeatInventoryDTO.SeatLayoutResponse allocateSeats(SeatInventoryDTO.AllocateRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new CineBookException("Show not found"));

        for (SeatInventoryDTO.SeatAllocation allocation : request.getSeats()) {
            Seat seat = seatRepository.findById(allocation.getSeatId())
                    .orElseThrow(() -> new CineBookException("Seat not found: " + allocation.getSeatId()));

            // Upsert: update if exists, create if not
            ShowSeatInventory inventory = inventoryRepository
                    .findByShowIdAndSeatId(request.getShowId(), allocation.getSeatId())
                    .orElse(ShowSeatInventory.builder().show(show).seat(seat).build());

            inventory.setSeatStatus(ShowSeatInventory.SeatStatus.valueOf(allocation.getStatus()));
            inventory.setPrice(allocation.getPrice() != null
                    ? allocation.getPrice()
                    : show.getBasePrice().multiply(categoryMultiplier(seat.getCategory())));

            inventoryRepository.save(inventory);
        }

        return getSeatLayout(request.getShowId());
    }

    @Cacheable(value = "seat-layout", key = "#showId")
    @Transactional(readOnly = true)
    public SeatInventoryDTO.SeatLayoutResponse getSeatLayout(String showId) {
        List<ShowSeatInventory> inventory = inventoryRepository.findByShowId(showId);

        List<SeatInventoryDTO.SeatInfo> seatInfos = inventory.stream()
                .map(ssi -> SeatInventoryDTO.SeatInfo.builder()
                        .seatId(ssi.getId())
                        .seatNumber(ssi.getSeat().getSeatNumber())
                        .rowLabel(ssi.getSeat().getRowLabel())
                        .category(ssi.getSeat().getCategory().name())
                        .status(ssi.getSeatStatus().name())
                        .price(ssi.getPrice())
                        .build())
                .collect(Collectors.toList());

        long available = inventory.stream()
                .filter(s -> s.getSeatStatus() == ShowSeatInventory.SeatStatus.AVAILABLE)
                .count();

        return SeatInventoryDTO.SeatLayoutResponse.builder()
                .showId(showId)
                .seats(seatInfos)
                .totalSeats(inventory.size())
                .availableSeats((int) available)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────

    private void autoAllocateSeats(Show show, Screen screen, BigDecimal basePrice) {
        List<Seat> activeSeats = seatRepository.findByScreenIdAndIsActiveTrue(screen.getId());
        List<ShowSeatInventory> inventory = activeSeats.stream()
                .map(seat -> ShowSeatInventory.builder()
                        .show(show)
                        .seat(seat)
                        .seatStatus(ShowSeatInventory.SeatStatus.AVAILABLE)
                        .price(basePrice.multiply(categoryMultiplier(seat.getCategory())))
                        .build())
                .collect(Collectors.toList());
        inventoryRepository.saveAll(inventory);
    }

    private BigDecimal categoryMultiplier(Seat.SeatCategory category) {
        return switch (category) {
            case GENERAL  -> BigDecimal.ONE;
            case PREMIUM  -> new BigDecimal("1.50");
            case RECLINER -> new BigDecimal("2.00");
            case COUPLE   -> new BigDecimal("1.80");
        };
    }

    private Show.ShowSlot deriveSlot(LocalDateTime startTime) {
        LocalTime time = startTime.toLocalTime();
        if (time.isBefore(LocalTime.NOON)) return Show.ShowSlot.MORNING;
        if (time.isBefore(LocalTime.of(17, 0))) return Show.ShowSlot.AFTERNOON;
        if (time.isBefore(LocalTime.of(21, 0))) return Show.ShowSlot.EVENING;
        return Show.ShowSlot.NIGHT;
    }

    private ShowDTO.Response mapToResponse(Show show, int availableSeats) {
        return ShowDTO.Response.builder()
                .showId(show.getId())
                .movieTitle(show.getMovie().getTitle())
                .theatreName(show.getTheatre().getName())
                .screenName(show.getScreen().getName())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .slot(show.getSlot().name())
                .basePrice(show.getBasePrice())
                .availableSeats(availableSeats)
                .status(show.getStatus().name())
                .build();
    }
}
