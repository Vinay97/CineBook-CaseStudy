package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {

    // Used by ShowManagementService during auto seat allocation
    List<Seat> findByScreenIdAndIsActiveTrue(String screenId);

    List<Seat> findByScreenId(String screenId);

    // Count active seats for capacity checks
    int countByScreenIdAndIsActiveTrue(String screenId);
}
