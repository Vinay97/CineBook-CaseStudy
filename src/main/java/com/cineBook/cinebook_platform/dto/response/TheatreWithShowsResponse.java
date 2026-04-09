package com.cineBook.cinebook_platform.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Response for GET /api/v1/browse/movies/{movieId}/theatres
 *
 * Groups all shows of a movie in a single theatre together.
 * The client gets one object per theatre, with all show timings inside —
 * matching how movie listing pages typically display results.
 *
 * Example structure:
 * [
 *   {
 *     "theatreId": "...",
 *     "theatreName": "PVR Juhu",
 *     "address": "...",
 *     "city": "Mumbai",
 *     "shows": [
 *       { "startTime": "10:00", "slot": "MORNING", "availableSeats": 120, ... },
 *       { "startTime": "14:00", "slot": "AFTERNOON", "availableSeats": 45, ... }
 *     ]
 *   },
 *   ...
 * ]
 */
@Value
@Builder
public class TheatreWithShowsResponse {

    String theatreId;
    String theatreName;
    String address;
    String city;
    String country;

    // All scheduled shows for the requested movie on the requested date
    List<ShowResponse> shows;
}
