package com.cineBook.cinebook_platform.controller;


import com.cineBook.cinebook_platform.auth.AuthService;
import com.cineBook.cinebook_platform.auth.dto.AuthResponse;
import com.cineBook.cinebook_platform.auth.dto.CreatePartnerRequest;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.Movie;
import com.cineBook.cinebook_platform.model.Theatre;
import com.cineBook.cinebook_platform.repository.MovieRepository;
import com.cineBook.cinebook_platform.repository.TheatreRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * PLATFORM_ADMIN only.
 * Responsible for: movie management, theatre account approval/suspension.
 * Does NOT manage screens, seats, or shows — those belong to the theatre partner.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class PlatformAdminController {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    private final AuthService authService;

    // ── Movie Management ──────────────────────────────────────

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieRepository.save(movie));
    }

    @PutMapping("/movies/{id}/status")
    public ResponseEntity<Movie> updateMovieStatus(
            @PathVariable String id,
            @RequestParam String status) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new CineBookException("Movie not found: " + id));
        movie.setStatus(Movie.MovieStatus.valueOf(status));
        return ResponseEntity.ok(movieRepository.save(movie));
    }

    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieRepository.findAll());
    }

    // ── Theatre Account Governance ────────────────────────────
    // Platform admin approves or suspends theatre partner accounts.
    // They do NOT create theatre details — the partner does that themselves.

    @PutMapping("/theatres/{id}/approve")
    public ResponseEntity<Theatre> approveTheatre(@PathVariable String id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new CineBookException("Theatre not found: " + id));
        if (theatre.getStatus() != Theatre.TheatreStatus.PENDING_APPROVAL) {
            throw new CineBookException("Theatre is not in PENDING_APPROVAL state");
        }
        theatre.setStatus(Theatre.TheatreStatus.ACTIVE);
        return ResponseEntity.ok(theatreRepository.save(theatre));
    }

    @PutMapping("/theatres/{id}/suspend")
    public ResponseEntity<Theatre> suspendTheatre(@PathVariable String id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new CineBookException("Theatre not found: " + id));
        theatre.setStatus(Theatre.TheatreStatus.SUSPENDED);
        return ResponseEntity.ok(theatreRepository.save(theatre));
    }

    @GetMapping("/theatres")
    public ResponseEntity<List<Theatre>> getAllTheatres(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(
                    theatreRepository.findByStatus(Theatre.TheatreStatus.valueOf(status)));
        }
        return ResponseEntity.ok(theatreRepository.findAll());
    }


    @PostMapping("/partners")
    public ResponseEntity<AuthResponse> createTheatrePartner(
            @Valid @RequestBody CreatePartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.createTheatrePartner(request));
    }

    // ── Request DTOs ──────────────────────────────────────────

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
        @NotBlank private String status;
    }
}
