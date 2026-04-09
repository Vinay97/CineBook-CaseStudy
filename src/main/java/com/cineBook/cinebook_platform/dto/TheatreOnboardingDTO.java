package com.cineBook.cinebook_platform.dto;

import lombok.*;

import java.util.List;


public class TheatreOnboardingDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TheatreResponse {
        private String id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String country;
        private String pincode;
        private String contactEmail;
        private String contactPhone;
        private String status;
        private Integer totalScreens;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenResponse {
        private String id;
        private String name;
        private Integer totalSeats;
        private String screenType;
        private String theatreId;
        private List<ShowSummary> shows;
    }

    @Data
    @Builder
    public static class ShowSummary {
        private String id;
        private String showDate;
        private String startTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatResponse {
        private String id;
        private String screenId;
        private String rowLabel;
        private Integer seatIndex;
        private String seatNumber;
        private String category;
        private Boolean isActive;
    }
}