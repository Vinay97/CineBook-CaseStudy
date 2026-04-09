package com.cineBook.cinebook_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// ─────────────────────────────────────────────────────────────
// THEATRE DTOs
// ─────────────────────────────────────────────────────────────

public class TheatreDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String address;
        private String city;
        private String country;
        private List<ScreenInfo> screens;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScreenInfo {
        private String screenId;
        private String screenName;
        private String screenType;
        private Integer totalSeats;
    }
}

// ─────────────────────────────────────────────────────────────
// SHOW DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// SEAT INVENTORY DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// BOOKING DTOs
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// BROWSE / SEARCH DTOs
// ─────────────────────────────────────────────────────────────

