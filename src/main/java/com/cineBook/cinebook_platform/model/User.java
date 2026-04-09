package com.cineBook.cinebook_platform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;      // BCrypt hashed

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String theatreId;     // only for THEATRE_PARTNER

    public enum UserRole {
        PLATFORM_ADMIN,
        THEATRE_PARTNER,
        CUSTOMER
    }
}