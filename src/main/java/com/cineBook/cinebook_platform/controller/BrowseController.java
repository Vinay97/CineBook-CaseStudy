package com.cineBook.cinebook_platform.controller;

// ─────────────────────────────────────────────────────────────
// B2C: Browse Controller
// ─────────────────────────────────────────────────────────────

import com.cineBook.cinebook_platform.dto.BrowseDTO;
import com.cineBook.cinebook_platform.dto.SeatInventoryDTO;
import com.cineBook.cinebook_platform.dto.ShowDTO;
import com.cineBook.cinebook_platform.service.BrowseService;
import com.cineBook.cinebook_platform.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/browse")
@RequiredArgsConstructor
public class BrowseController {

    private final BrowseService browseService;

    private final ShowManagementService showManagementService;

    /**
     * GET /api/v1/browse/movies/{movieId}/theatres
     * ?city=Mumbai&date=2024-12-25&language=Hindi
     *
     * Browse: theatres currently running a show in a city with timings
     */
    @GetMapping("/movies/{movieId}/theatres")
    public ResponseEntity<List<BrowseDTO.TheatreWithShows>> browseTheatresForMovie(
            @PathVariable String movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String language) {

        return ResponseEntity.ok(browseService.browseTheatresForMovie(movieId, city, date, language));
    }

    /**
     * GET /api/v1/browse/offers?city=Mumbai&date=2024-12-25
     *
     * Platform offers applicable in a city today
     */
    @GetMapping("/offers")
    public ResponseEntity<List<ShowDTO.OfferInfo>> getOffersForCity(
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(browseService.getOffersForCity(city, date));
    }

    @GetMapping("/movies")
    public ResponseEntity<List<BrowseDTO.MovieInfo>> browseMovies(
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(browseService.browseMovies(city, date, status));
    }

    @GetMapping("/shows/{showId}")
    public ResponseEntity<SeatInventoryDTO.SeatLayoutResponse> getSeatLayout(
            @PathVariable String showId) {

        return ResponseEntity.ok(showManagementService.getSeatLayout(showId));
    }
}
