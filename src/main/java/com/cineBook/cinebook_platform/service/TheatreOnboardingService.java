package com.cineBook.cinebook_platform.service;

import com.cineBook.cinebook_platform.controller.TheatreOnboardingController;
import com.cineBook.cinebook_platform.dto.TheatreOnboardingDTO;
import com.cineBook.cinebook_platform.dto.request.AddScreenRequest;
import com.cineBook.cinebook_platform.dto.request.GenerateSeatsRequest;
import com.cineBook.cinebook_platform.dto.request.RegisterTheatreRequest;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.Screen;
import com.cineBook.cinebook_platform.model.Seat;
import com.cineBook.cinebook_platform.model.Theatre;
import com.cineBook.cinebook_platform.model.User;
import com.cineBook.cinebook_platform.repository.ScreenRepository;
import com.cineBook.cinebook_platform.repository.SeatRepository;
import com.cineBook.cinebook_platform.repository.TheatreRepository;
import com.cineBook.cinebook_platform.repository.UserRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheatreOnboardingService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public TheatreOnboardingDTO.TheatreResponse registerTheatre(RegisterTheatreRequest req, String userId) {
        User partner = userRepository.findById(userId)
                .orElseThrow(() -> new CineBookException("User not found"));

        if (partner.getTheatreId() != null) {
            throw new CineBookException("You have already registered a theatre. Contact platform admin to register an additional one.");
        }

        Theatre theatre = Theatre.builder()
                .name(req.getName())
                .address(req.getAddress())
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry())
                .pincode(req.getPincode())
                .contactEmail(req.getContactEmail())
                .contactPhone(req.getContactPhone())
                .status(Theatre.TheatreStatus.PENDING_APPROVAL)
                .build();

        Theatre saved = theatreRepository.save(theatre);

        partner.setTheatreId(saved.getId());
        userRepository.save(partner);

        return buildTheatreResponse(saved);
    }

    private TheatreOnboardingDTO.TheatreResponse buildTheatreResponse(Theatre theatre) {
        return TheatreOnboardingDTO.TheatreResponse.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .address(theatre.getAddress())
                .city(theatre.getCity())
                .state(theatre.getState())
                .country(theatre.getCountry())
                .pincode(theatre.getPincode())
                .contactEmail(theatre.getContactEmail())
                .contactPhone(theatre.getContactPhone())
                .status(theatre.getStatus().name())
                .totalScreens(theatre.getScreens() != null ? theatre.getScreens().size() : 0)
                .build();
    }

    @Transactional(readOnly = true)
    public TheatreOnboardingDTO.TheatreResponse getMyTheatre(String userId) {
        String theatreId = getTheatreIdForPartner(userId);
        Theatre t = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new CineBookException("Theatre not found"));

        return buildTheatreResponse(t);
    }

    @Transactional
    public TheatreOnboardingDTO.ScreenResponse addScreen(AddScreenRequest req, String userId) {
        String theatreId = getTheatreIdForPartner(userId);
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new CineBookException("Theatre not found"));

        if (theatre.getStatus() != Theatre.TheatreStatus.ACTIVE) {
            throw new CineBookException("Theatre must be ACTIVE before adding screens. Current status: " + theatre.getStatus());
        }

        Screen screen = Screen.builder()
                .theatre(theatre)
                .name(req.getName())
                .totalSeats(req.getTotalSeats())
                .screenType(Screen.ScreenType.valueOf(req.getScreenType()))
                .build();

        screenRepository.save(screen);
        return buildScreenResponse(screen);
    }

    private TheatreOnboardingDTO.ScreenResponse buildScreenResponse(Screen screen) {
        List<TheatreOnboardingDTO.ShowSummary> showSummaries = (screen.getShows() != null)
                ? screen.getShows().stream()
                    .map(s -> TheatreOnboardingDTO.ShowSummary.builder()
                            .id(s.getId())
                            .showDate(s.getShowDate().toString())
                            .startTime(s.getStartTime().toString())
                            .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();
        return TheatreOnboardingDTO.ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .screenType(screen.getScreenType().name())
                .theatreId(screen.getTheatre().getId())
                .shows(showSummaries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TheatreOnboardingDTO.ScreenResponse> getMyScreens(String userId) {
        String theatreId = getTheatreIdForPartner(userId);
        List<Screen> screens = screenRepository.findByTheatreId(theatreId);
        return screens.stream().map(this::buildScreenResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<TheatreOnboardingDTO.SeatResponse> generateSeats(String screenId, GenerateSeatsRequest req, String userId) {
        String theatreId = getTheatreIdForPartner(userId);
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new CineBookException("Screen not found"));

        if (!screen.getTheatre().getId().equals(theatreId)) {
            throw new CineBookException("Unauthorised: this screen does not belong to your theatre");
        }

        List<Seat> seats = new ArrayList<>();
        for (char row = req.getFromRow().charAt(0); row <= req.getToRow().charAt(0); row++) {
            Seat.SeatCategory category = resolveCategory(row, req.getFromRow().charAt(0), req.getToRow().charAt(0));
            for (int i = 1; i <= req.getSeatsPerRow(); i++) {
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
        return seats.stream().map(this::buildSeatResponse).collect(Collectors.toList());
    }

    private TheatreOnboardingDTO.SeatResponse buildSeatResponse(Seat seat) {
        return TheatreOnboardingDTO.SeatResponse.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .rowLabel(seat.getRowLabel())
                .seatIndex(seat.getSeatIndex())
                .seatNumber(seat.getSeatNumber())
                .category(seat.getCategory().name())
                .isActive(seat.getIsActive())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TheatreOnboardingDTO.SeatResponse> getSeatsForScreen(String screenId, String userId) {
        String theatreId = getTheatreIdForPartner(userId);
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new CineBookException("Screen not found"));

        if (!screen.getTheatre().getId().equals(theatreId)) {
            throw new CineBookException("Unauthorised: this screen does not belong to your theatre");
        }

        List<Seat> s = seatRepository.findByScreenId(screenId);
        return s.stream().map(this::buildSeatResponse).collect(Collectors.toList());
    }

    private String getTheatreIdForPartner(String userId) {
        User partner = userRepository.findById(userId)
                .orElseThrow(() -> new CineBookException("User not found"));

        if (partner.getTheatreId() == null) {
            throw new CineBookException("No theatre registered for this partner yet. Please register your theatre first via POST /theatre-admin/theatre");
        }
        return partner.getTheatreId();
    }

    private Seat.SeatCategory resolveCategory(char row, char first, char last) {
        int total = last - first + 1;
        int rowIndex = row - first;
        return (rowIndex >= total - 2)
                ? Seat.SeatCategory.PREMIUM
                : Seat.SeatCategory.GENERAL;
    }
}