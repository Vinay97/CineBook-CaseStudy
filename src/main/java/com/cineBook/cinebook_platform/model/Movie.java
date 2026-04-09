package com.cineBook.cinebook_platform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String language;
    private String genre;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private Double rating;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Show> shows;

    public enum MovieStatus { UPCOMING, NOW_PLAYING, ENDED }
}
