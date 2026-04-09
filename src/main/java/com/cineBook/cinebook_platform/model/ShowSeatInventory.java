package com.cineBook.cinebook_platform.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "show_seat_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;

    private BigDecimal price; // can vary from show's base price based on category

    @Version  // Optimistic locking - crucial for concurrent booking
    private Long version;

    public enum SeatStatus { AVAILABLE, LOCKED, BOOKED, BLOCKED }
}
