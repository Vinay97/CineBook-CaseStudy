package com.cineBook.cinebook_platform.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings",
        indexes = @Index(name = "idx_booking_user", columnList = "user_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String bookingReference; // human-readable e.g. CB-2024-00123

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "user_id", nullable = false)
    private String userId; // from Auth service / JWT

    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BigDecimal convenienceFee;
    private BigDecimal totalAmount;

    private String paymentId;      // from Payment Gateway
    private String paymentStatus;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookedSeat> bookedSeats;

    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED, FAILED }
}
