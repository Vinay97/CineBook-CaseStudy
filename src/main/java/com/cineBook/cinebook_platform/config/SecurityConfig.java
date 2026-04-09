package com.cineBook.cinebook_platform.config;


import com.cineBook.cinebook_platform.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {


    // Reads from application-local.properties or application.properties
    // Defaults to true so prod never accidentally runs unsecured
    @Value("${app.security.jwt-enabled:true}")
    private boolean jwtEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        // Always disable CSRF — stateless REST API does not need it
        http.csrf(AbstractHttpConfigurer::disable);

        // Allow H2 console frames — only needed locally but harmless elsewhere
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));

        if (!jwtEnabled) {
            // JWT disabled — permit everything, no token required
            // Flip app.security.jwt-enabled=true in application-local.properties
            // to switch this on without changing any code
            log.warn("⚠ JWT authentication is DISABLED. " +
                    "Set app.security.jwt-enabled=true to enable it.");

            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        } else {
            // JWT enabled — full role-based security
            log.info("✔ JWT authentication is ENABLED.");

            http
                    .sessionManagement(s ->
                            s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            // Public — no token needed
                            .requestMatchers(
                                    "/api/v1/auth/**",
                                    "/api/v1/browse/**",
                                    "/actuator/health",
                                    "/swagger-ui/**",
                                    "/api-docs/**",
                                    "/h2-console/**",
                                    "/local/**",           // LocalTokenController
                                    "/mock-payment/**"     // MockPaymentController
                            ).permitAll()
                            // Role-based
                            .requestMatchers("/api/v1/admin/**")
                            .hasRole("PLATFORM_ADMIN")
                            .requestMatchers("/api/v1/theatre-admin/**")
                            .hasRole("THEATRE_PARTNER")
                            // Everything else needs a valid token
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthFilter,
                            UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}