package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.model.*;
import com.cineBook.cinebook_platform.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Platform admin endpoints for populating master data.
 *
 * In production these would be secured with ROLE_ADMIN.
 * In local (H2) mode, SecurityConfig permits all — so these
 * are callable directly without any token.
 *
 * Base path: /api/v1/admin
 */
@RestController
@RequestMapping("/api/v1/admin-deprecated")
@RequiredArgsConstructor
public class AdminController {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    // ─────────────────────────────────────────────────────────
    // MOVIE
    // ─────────────────────────────────────────────────────────

    @PostMapping("/movies")
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody CreateMovieRequest req) {
        Movie movie = Movie.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .language(req.getLanguage())
                .genre(req.getGenre())
                .durationMinutes(req.getDurationMinutes())
                .releaseDate(req.getReleaseDate())
                .posterUrl(req.getPosterUrl())
                .rating(req.getRating())
                .status(Movie.MovieStatus.valueOf(req.getStatus()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(movieRepository.save(movie));
    }

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieRepository.findAll());
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable String id) {
        return movieRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────
    // THEATRE
    // ─────────────────────────────────────────────────────────

    @PostMapping("/theatres")
    public ResponseEntity<Theatre> createTheatre(@Valid @RequestBody CreateTheatreRequest req) {
        Theatre theatre = Theatre.builder()
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry())
                .pincode(req.getPincode())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .status(Theatre.TheatreStatus.ACTIVE)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(theatreRepository.save(theatre));
    }

    @GetMapping("/theatres")
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        return ResponseEntity.ok(theatreRepository.findAll());
    }

    // ─────────────────────────────────────────────────────────
    // SCREEN  (belongs to a Theatre)
    // ─────────────────────────────────────────────────────────

    @PostMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<Screen> createScreen(
            @PathVariable String theatreId,
            @Valid @RequestBody CreateScreenRequest req) {

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found: " + theatreId));

        Screen screen = Screen.builder()
                .theatre(theatre)
                .name(req.getName())
                .totalSeats(req.getTotalSeats())
                .screenType(Screen.ScreenType.valueOf(req.getScreenType()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(screenRepository.save(screen));
    }

    @GetMapping("/theatres/{theatreId}/screens")
    public ResponseEntity<List<Screen>> getScreensForTheatre(@PathVariable String theatreId) {
        return ResponseEntity.ok(screenRepository.findByTheatreId(theatreId));
    }

    // ─────────────────────────────────────────────────────────
    // SEATS  (belongs to a Screen)
    // Bulk create — pass a list of seat definitions
    // ─────────────────────────────────────────────────────────

    @PostMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<Seat>> createSeats(
            @PathVariable String screenId,
            @Valid @RequestBody CreateSeatsRequest req) {

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new RuntimeException("Screen not found: " + screenId));

        List<Seat> seats = req.getSeats().stream()
                .map(s -> Seat.builder()
                        .screen(screen)
                        .seatNumber(s.getSeatNumber())
                        .rowLabel(s.getRowLabel())
                        .seatIndex(s.getSeatIndex())
                        .category(Seat.SeatCategory.valueOf(s.getCategory()))
                        .isActive(true)
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(seatRepository.saveAll(seats));
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<Seat>> getSeatsForScreen(@PathVariable String screenId) {
        return ResponseEntity.ok(seatRepository.findByScreenId(screenId));
    }

    // ─────────────────────────────────────────────────────────
    // Convenience: generate seats automatically for a screen
    // e.g. rows A-J, 10 seats each → 100 seats
    // ─────────────────────────────────────────────────────────

    @PostMapping("/screens/{screenId}/seats/generate")
    public ResponseEntity<List<Seat>> generateSeats(
            @PathVariable String screenId,
            @Valid @RequestBody GenerateSeatsRequest req) {

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new RuntimeException("Screen not found: " + screenId));

        List<Seat> seats = new java.util.ArrayList<>();
        for (char row = req.getFromRow().charAt(0); row <= req.getToRow().charAt(0); row++) {
            Seat.SeatCategory cat = resolveCategory(row, req.getFromRow().charAt(0), req.getToRow().charAt(0));
            for (int idx = 1; idx <= req.getSeatsPerRow(); idx++) {
                seats.add(Seat.builder()
                        .screen(screen)
                        .rowLabel(String.valueOf(row))
                        .seatIndex(idx)
                        .seatNumber(row + String.valueOf(idx))
                        .category(cat)
                        .isActive(true)
                        .build());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(seatRepository.saveAll(seats));
    }

    // Last 2 rows = PREMIUM, everything else = GENERAL
    private Seat.SeatCategory resolveCategory(char row, char first, char last) {
        int total = last - first + 1;
        int rowIndex = row - first;
        return (rowIndex >= total - 2) ? Seat.SeatCategory.PREMIUM : Seat.SeatCategory.GENERAL;
    }

    // ─────────────────────────────────────────────────────────
    // Request DTOs (inner classes — admin-only, not shared)
    // ─────────────────────────────────────────────────────────

    @Data
    public static class CreateMovieRequest {
        @NotBlank private String title;
        private String description;
        @NotBlank private String language;
        @NotBlank private String genre;
        @NotNull @Min(1) private Integer durationMinutes;
        private LocalDate releaseDate;
        private String posterUrl;
        private Double rating;
        @NotBlank private String status; // NOW_PLAYING, UPCOMING, ENDED
    }

    @Data
    public static class CreateTheatreRequest {
        @NotBlank private String name;
        @NotBlank private String address;
        @NotBlank private String city;
        private String state;
        @NotBlank private String country;
        private String pincode;
        private String contactEmail;
        private String contactPhone;
    }

    @Data
    public static class CreateScreenRequest {
        @NotBlank private String name;
        @NotNull @Min(1) private Integer totalSeats;
        @NotBlank private String screenType; // STANDARD, IMAX, DOLBY, FOUR_DX
    }

    @Data
    public static class CreateSeatsRequest {
        @NotEmpty private List<SeatDef> seats;

        @Data
        public static class SeatDef {
            @NotBlank private String seatNumber; // e.g. "A1"
            @NotBlank private String rowLabel;   // e.g. "A"
            @NotNull  private Integer seatIndex; // e.g. 1
            @NotBlank private String category;   // GENERAL, PREMIUM, RECLINER, COUPLE
        }
    }

    @Data
    public static class GenerateSeatsRequest {
        @NotBlank private String fromRow;    // e.g. "A"
        @NotBlank private String toRow;      // e.g. "J"
        @NotNull @Min(1) private Integer seatsPerRow; // e.g. 10
    }
}
