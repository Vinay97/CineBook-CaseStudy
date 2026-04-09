package com.cineBook.cinebook_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// ─────────────────────────────────────────────────────────────
// THEATRE DTOs
// ─────────────────────────────────────────────────────────────

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

public class BrowseDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TheatreShowsRequest {
        private String movieId;
        private String city;
        private LocalDate date;
        private String language;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TheatreWithShows {
        private String theatreId;
        private String theatreName;
        private String address;
        private String city;
        private List<ShowDTO.Response> shows;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MovieInfo {
        private String movieId;
        private String title;
        private String genre;
        private String language;
        private String posterUrl;
        private Integer durationMinutes;
        private LocalDate releaseDate;
        private Double rating;
        private String status;
    }
}
