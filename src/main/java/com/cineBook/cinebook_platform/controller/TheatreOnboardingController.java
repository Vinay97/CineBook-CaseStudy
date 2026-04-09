package com.cineBook.cinebook_platform.controller;


import com.cineBook.cinebook_platform.dto.TheatreOnboardingDTO;
import com.cineBook.cinebook_platform.dto.request.AddScreenRequest;
import com.cineBook.cinebook_platform.dto.request.GenerateSeatsRequest;
import com.cineBook.cinebook_platform.dto.request.RegisterTheatreRequest;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.Screen;
import com.cineBook.cinebook_platform.model.Seat;
import com.cineBook.cinebook_platform.model.Theatre;
import com.cineBook.cinebook_platform.model.User;
import com.cineBook.cinebook_platform.repository.ScreenRepository;
import com.cineBook.cinebook_platform.repository.SeatRepository;
import com.cineBook.cinebook_platform.repository.TheatreRepository;
import com.cineBook.cinebook_platform.repository.UserRepository;
import com.cineBook.cinebook_platform.service.TheatreOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * THEATRE_PARTNER only.
 * Responsible for: registering their own theatre, adding screens,
 * defining seat layouts. Partners can only manage their own theatre.
 */
@RestController
@RequestMapping("/api/v1/theatre-admin")
@RequiredArgsConstructor
public class TheatreOnboardingController {

    private final TheatreOnboardingService theatreOnboardingService;

    // ── Theatre Registration ──────────────────────────────────
    // Partner registers their own theatre. Status starts as PENDING_APPROVAL.
    // Platform admin then approves it via PUT /api/v1/admin/theatres/{id}/approve.

    @PostMapping("/theatre")
    public ResponseEntity<TheatreOnboardingDTO.TheatreResponse> registerTheatre(
            @Valid @RequestBody RegisterTheatreRequest req,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(theatreOnboardingService.registerTheatre(req, userId));
    }

    @GetMapping("/theatre")
    public ResponseEntity<TheatreOnboardingDTO.TheatreResponse> getMyTheatre(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(theatreOnboardingService.getMyTheatre(userId));
    }

    // ── Screen Management ─────────────────────────────────────

    @PostMapping("/screens")
    public ResponseEntity<TheatreOnboardingDTO.ScreenResponse> addScreen(
            @Valid @RequestBody AddScreenRequest req,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreOnboardingService.addScreen(req, userId));
    }

    @GetMapping("/screens")
    public ResponseEntity<List<TheatreOnboardingDTO.ScreenResponse>> getMyScreens(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(theatreOnboardingService.getMyScreens(userId));
    }

    // ── Seat Management ───────────────────────────────────────

    @PostMapping("/screens/{screenId}/seats/generate")
    public ResponseEntity<List<TheatreOnboardingDTO.SeatResponse>> generateSeats(
            @PathVariable String screenId,
            @Valid @RequestBody GenerateSeatsRequest req,
            @AuthenticationPrincipal String userId) {

       return ResponseEntity.ok(theatreOnboardingService.generateSeats(screenId, req, userId));
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<List<TheatreOnboardingDTO.SeatResponse>> getSeatsForScreen(
            @PathVariable String screenId,
            @AuthenticationPrincipal String userId) {

       return ResponseEntity.ok(theatreOnboardingService.getSeatsForScreen(screenId, userId));
    }


}
