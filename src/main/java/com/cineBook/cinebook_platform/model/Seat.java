package com.cineBook.cinebook_platform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private String seatNumber;  // e.g., "A1", "B12"

    private String rowLabel;    // e.g., "A"
    private Integer seatIndex;  // e.g., 1

    @Enumerated(EnumType.STRING)
    private SeatCategory category;

    private Boolean isActive;

    public enum SeatCategory { GENERAL, PREMIUM, RECLINER, COUPLE }
}