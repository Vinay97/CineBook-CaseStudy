package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    // Find all movies currently playing in a city
    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN m.shows s
        JOIN s.theatre t
        WHERE t.city = :city
          AND m.status = 'NOW_PLAYING'
        ORDER BY m.title
        """)
    List<Movie> findNowPlayingByCity(@Param("city") String city);

    // Find by language and genre filters (for browse screen)
    List<Movie> findByLanguageAndGenreAndStatus(
            String language, String genre, Movie.MovieStatus status);

    // Full-text style search by title (case-insensitive)
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Movie> searchByTitle(@Param("title") String title);

    List<Movie> findByStatus(Movie.MovieStatus status);
}
