package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.ShowSeatInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatInventoryRepository extends JpaRepository<ShowSeatInventory, String> {

    List<ShowSeatInventory> findByShowId(String showId);

    /**
     * Pessimistic write lock on specific seats for a show.
     * Used during the booking flow to prevent double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT ssi FROM ShowSeatInventory ssi
        WHERE ssi.show.id = :showId
          AND ssi.id IN :seatIds
          AND ssi.seatStatus = 'AVAILABLE'
        """)
    List<ShowSeatInventory> findAvailableSeatsWithLock(
            @Param("showId") String showId,
            @Param("seatIds") List<String> seatIds);

    @Query("SELECT COUNT(ssi) FROM ShowSeatInventory ssi WHERE ssi.show.id = :showId AND ssi.seatStatus = 'AVAILABLE'")
    Integer countAvailableSeats(@Param("showId") String showId);

    @Modifying
    @Query("UPDATE ShowSeatInventory ssi SET ssi.seatStatus = :status WHERE ssi.id IN :ids")
    void updateStatusForSeats(@Param("ids") List<String> ids,
                              @Param("status") ShowSeatInventory.SeatStatus status);

    java.util.Optional<ShowSeatInventory> findByShowIdAndSeatId(String showId, String seatId);
}
