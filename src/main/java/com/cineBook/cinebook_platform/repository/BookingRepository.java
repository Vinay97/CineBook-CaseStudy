package com.cineBook.cinebook_platform.repository;


import com.cineBook.cinebook_platform.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(String userId);

    List<Booking> findByIdInAndUserId(List<String> ids, String userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.id = :showId AND b.status = 'CONFIRMED'")
    long countConfirmedBookingsForShow(@Param("showId") String showId);
}
