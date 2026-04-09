package com.cineBook.cinebook_platform.repository;

import com.cineBook.cinebook_platform.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, String> {

    List<Screen> findByTheatreId(String theatreId);

    List<Screen> findByTheatreIdAndScreenType(String theatreId, Screen.ScreenType screenType);
}
