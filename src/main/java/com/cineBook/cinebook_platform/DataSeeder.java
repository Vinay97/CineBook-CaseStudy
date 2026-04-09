package com.cineBook.cinebook_platform;

import com.cineBook.cinebook_platform.dto.ShowDTO;
import com.cineBook.cinebook_platform.model.*;
import com.cineBook.cinebook_platform.repository.*;
import com.cineBook.cinebook_platform.service.ShowManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowManagementService showManagementService;

    private Theatre seedUsers() {
        Theatre theatre = theatreRepository.save(
                Theatre.builder()
                        .name("PVR Juhu")
                        .address("Juhu Tara Road, Juhu")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .status(Theatre.TheatreStatus.ACTIVE)
                        .build()
        );

        userRepository.saveAll(List.of(
                User.builder()
                        .email("admin@cinebook.com")
                        .name("Platform Admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(User.UserRole.PLATFORM_ADMIN)
                        .build(),
                User.builder()
                        .email("partner@pvr.com")
                        .name("PVR Theatre Partner")
                        .password(passwordEncoder.encode("Partner@123"))
                        .role(User.UserRole.THEATRE_PARTNER)
                        .theatreId(theatre.getId())
                        .build(),
                User.builder()
                        .email("customer@gmail.com")
                        .name("Rahul Kumar")
                        .password(passwordEncoder.encode("Customer@123"))
                        .role(User.UserRole.CUSTOMER)
                        .build()
        ));
        return theatre;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] Data already exists, skipping seed.");
            return;
        }

        log.info("[DataSeeder] Seeding initial data...");
        Theatre theatre = seedUsers();
        seedMovies();
        Screen screen = seedScreenAndSeatsIfNeeded(theatre);
        seedShowsForToday(theatre, screen);
        log.info("[DataSeeder] Done.");
    }

    private void seedMovies() {
        movieRepository.saveAll(List.of(
                Movie.builder()
                        .title("Kalki 2898 AD")
                        .language("Hindi")
                        .genre("Action")
                        .durationMinutes(180)
                        .releaseDate(LocalDate.of(2024, 6, 27))
                        .status(Movie.MovieStatus.NOW_PLAYING)
                        .rating(8.2)
                        .posterUrl(
                                "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=480&auto=format&fit=crop&q=80")
                        .build(),
                Movie.builder()
                        .title("Stree 2")
                        .language("Hindi")
                        .genre("Comedy Horror")
                        .durationMinutes(150)
                        .releaseDate(LocalDate.of(2024, 8, 15))
                        .status(Movie.MovieStatus.NOW_PLAYING)
                        .rating(8.8)
                        .posterUrl(
                                "https://images.unsplash.com/photo-1596727147705-61a532a659bd?w=480&auto=format&fit=crop&q=80")
                        .build()
        ));
        log.info("[DataSeeder] Movies created.");
    }

    private Screen seedScreenAndSeatsIfNeeded(Theatre theatre) {
        List<Screen> existing = screenRepository.findByTheatreId(theatre.getId());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        Screen screen = screenRepository.save(
                Screen.builder()
                        .theatre(theatre)
                        .name("Screen 1")
                        .totalSeats(60)
                        .screenType(Screen.ScreenType.STANDARD)
                        .build()
        );

        List<Seat> seats = new ArrayList<>();
        for (char row = 'A'; row <= 'F'; row++) {
            Seat.SeatCategory category = (row >= 'E')
                    ? Seat.SeatCategory.PREMIUM
                    : Seat.SeatCategory.GENERAL;
            for (int i = 1; i <= 10; i++) {
                seats.add(Seat.builder()
                        .screen(screen)
                        .rowLabel(String.valueOf(row))
                        .seatIndex(i)
                        .seatNumber(row + String.valueOf(i))
                        .category(category)
                        .isActive(true)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
        log.info("[DataSeeder] Screen + {} seats for {}", seats.size(), theatre.getName());
        return screen;
    }

    private void seedShowsForToday(Theatre theatre, Screen screen) {
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        int[] hours = {11, 15, 19};

        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            int hour = hours[i % hours.length];
            try {
                showManagementService.createShow(ShowDTO.CreateRequest.builder()
                        .movieId(m.getId())
                        .theatreId(theatre.getId())
                        .screenId(screen.getId())
                        .showDate(today)
                        .startTime(today.atTime(hour, 0))
                        .basePrice(new BigDecimal("320"))
                        .build());
                log.info("[DataSeeder] Show for '{}' at {}:00", m.getTitle(), hour);
            } catch (Exception e) {
                log.warn("[DataSeeder] Could not create show for {}: {}", m.getTitle(), e.getMessage());
            }
        }
    }
}
