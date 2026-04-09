package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.dto.SeatInventoryDTO;
import com.cineBook.cinebook_platform.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/theatre-admin/inventory")
@RequiredArgsConstructor
public class SeatInventoryController {

    private final ShowManagementService showManagementService;

    /**
     * POST /api/v1/theatre-admin/inventory/allocate
     * Allocate / update seat inventory for a show
     */
    @PostMapping("/allocate")
    public ResponseEntity<SeatInventoryDTO.SeatLayoutResponse> allocateSeats(
            @RequestBody SeatInventoryDTO.AllocateRequest request) {

        return ResponseEntity.ok(showManagementService.allocateSeats(request));
    }

    /**
     * GET /api/v1/theatre-admin/inventory/shows/{showId}
     * Get current seat layout and availability for a show
     */
}
