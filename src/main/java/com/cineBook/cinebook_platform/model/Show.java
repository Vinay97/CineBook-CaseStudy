package com.cineBook.cinebook_platform.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shows",
        indexes = {
                @Index(name = "idx_show_movie_date", columnList = "movie_id, show_date"),
                @Index(name = "idx_show_theatre_date", columnList = "theatre_id, show_date")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private LocalDate showDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ShowSlot slot; // MORNING, AFTERNOON, EVENING, NIGHT

    @Enumerated(EnumType.STRING)
    private ShowStatus status;

    private BigDecimal basePrice;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    private List<ShowSeatInventory> seatInventory;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    public enum ShowSlot {
        MORNING,    // Before 12:00
        AFTERNOON,  // 12:00 - 17:00
        EVENING,    // 17:00 - 21:00
        NIGHT       // After 21:00
    }

    public enum ShowStatus { SCHEDULED, CANCELLED, COMPLETED }
}
