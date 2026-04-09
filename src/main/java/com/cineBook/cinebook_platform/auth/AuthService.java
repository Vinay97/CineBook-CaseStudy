package com.cineBook.cinebook_platform.auth;


import com.cineBook.cinebook_platform.auth.dto.AuthResponse;
import com.cineBook.cinebook_platform.auth.dto.CreatePartnerRequest;
import com.cineBook.cinebook_platform.auth.dto.LoginRequest;
import com.cineBook.cinebook_platform.auth.dto.RegisterRequest;
import com.cineBook.cinebook_platform.exception.CineBookException;
import com.cineBook.cinebook_platform.model.User;
import com.cineBook.cinebook_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CineBookException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.CUSTOMER) // self-registration always creates CUSTOMER
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CineBookException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CineBookException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // Add to AuthService

    public AuthResponse createTheatrePartner(CreatePartnerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CineBookException(
                    "Email already registered: " + request.getEmail());
        }

        User partner = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.THEATRE_PARTNER)
                .theatreId(null) // they register their theatre separately
                .build();

        User saved = userRepository.save(partner);
        String token = jwtUtil.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }
}
