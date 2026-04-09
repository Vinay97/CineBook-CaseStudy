package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

    /**
     * Browse: all shows for a movie in a city on a given date.
     * Used for the "Browse theatres currently running the show" scenario.
     */
    @Query("""
        SELECT s FROM Show s
        JOIN FETCH s.theatre t
        JOIN FETCH s.movie m
        JOIN FETCH s.screen sc
        WHERE m.id = :movieId
          AND t.city = :city
          AND s.showDate = :date
          AND s.status = 'SCHEDULED'
        ORDER BY t.name, s.startTime
        """)
    List<Show> findShowsByMovieCityAndDate(
            @Param("movieId") String movieId,
            @Param("city") String city,
            @Param("date") LocalDate date);

    @Query("""
        SELECT s FROM Show s
        JOIN FETCH s.theatre t
        JOIN FETCH s.movie m
        JOIN FETCH s.screen sc
        WHERE t.city = :city
          AND s.showDate = :date
          AND s.status = :status
        ORDER BY t.name, s.startTime
        """)
    List<Show> findShowsByCityAndDateAndStatus(
            @Param("city") String city, @Param("date") LocalDate date,
            @Param("status") Show.ShowStatus status);

    /**
     * Variant: filter by language too
     */
    @Query("""
        SELECT s FROM Show s
        JOIN FETCH s.theatre t
        JOIN FETCH s.movie m
        JOIN FETCH s.screen sc
        WHERE m.id = :movieId
          AND t.city = :city
          AND s.showDate = :date
          AND m.language = :language
          AND s.status = 'SCHEDULED'
        ORDER BY t.name, s.startTime
        """)
    List<Show> findShowsByMovieCityDateAndLanguage(
            @Param("movieId") String movieId,
            @Param("city") String city,
            @Param("date") LocalDate date,
            @Param("language") String language);

    List<Show> findByTheatreIdAndShowDateAndStatus(
            String theatreId, LocalDate date, Show.ShowStatus status);
}