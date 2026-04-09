package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, String> {

    // Find all active theatres in a city
    List<Theatre> findByCityAndStatus(String city, Theatre.TheatreStatus status);

    // Find all active theatres across a country
    List<Theatre> findByCountryAndStatus(String country, Theatre.TheatreStatus status);

    // Find theatres showing a specific movie in a city
    @Query("""
        SELECT DISTINCT t FROM Theatre t
        JOIN t.shows s
        WHERE s.movie.id = :movieId
          AND t.city = :city
          AND t.status = 'ACTIVE'
          AND s.status = 'SCHEDULED'
        ORDER BY t.name
        """)
    List<Theatre> findTheatresShowingMovieInCity(
            @Param("movieId") String movieId,
            @Param("city") String city);

    boolean existsByNameAndCity(String name, String city);

    // Add to TheatreRepository
    List<Theatre> findByStatus(Theatre.TheatreStatus status);
}
