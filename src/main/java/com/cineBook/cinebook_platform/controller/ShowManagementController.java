package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.dto.ShowDTO;
import com.cineBook.cinebook_platform.repository.MovieRepository;
import com.cineBook.cinebook_platform.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/theatre-admin/shows")
@RequiredArgsConstructor
public class ShowManagementController {

    private final ShowManagementService showManagementService;
    private final MovieRepository movieRepository;

    /**
     * Movies available for scheduling shows (same data as platform catalog, partner-scoped URL).
     */
    @GetMapping("/catalog/movies")
    public ResponseEntity<List<MovieCatalogItem>> catalogMovies() {
        return ResponseEntity.ok(
                movieRepository.findAll().stream()
                        .map(m -> new MovieCatalogItem(
                                m.getId(),
                                m.getTitle(),
                                m.getLanguage(),
                                m.getStatus().name()))
                        .toList());
    }

    public record MovieCatalogItem(String id, String title, String language, String status) {
    }

    /**
     * POST /api/v1/theatre-admin/shows
     * Create a show for the day
     */
    @PostMapping
    public ResponseEntity<ShowDTO.Response> createShow(
            @RequestBody ShowDTO.CreateRequest request) {

        return ResponseEntity.ok(showManagementService.createShow(request));
    }

    /**
     * PUT /api/v1/theatre-admin/shows/{showId}
     * Update show timing / price / status
     */
    @PutMapping("/{showId}")
    public ResponseEntity<ShowDTO.Response> updateShow(
            @PathVariable String showId,
            @RequestBody ShowDTO.UpdateRequest request) {

        return ResponseEntity.ok(showManagementService.updateShow(showId, request));
    }

    /**
     * DELETE /api/v1/theatre-admin/shows/{showId}
     * Cancel a show
     */
    @DeleteMapping("/{showId}")
    public ResponseEntity<Void> cancelShow(@PathVariable String showId) {
        showManagementService.cancelShow(showId);
        return ResponseEntity.noContent().build();
    }
}
