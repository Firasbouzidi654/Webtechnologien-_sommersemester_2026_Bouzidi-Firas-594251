package de.htw_berlin.KinderCareConnect.service;

import de.htw_berlin.KinderCareConnect.entity.UserEntity;
import de.htw_berlin.KinderCareConnect.exception.EmailAlreadyExistsException;
import de.htw_berlin.KinderCareConnect.exception.InvalidCredentialsException;
import de.htw_berlin.KinderCareConnect.model.AuthResponse;
import de.htw_berlin.KinderCareConnect.model.LoginRequest;
import de.htw_berlin.KinderCareConnect.model.SignupRequest;
import de.htw_berlin.KinderCareConnect.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private static final List<String> VALID_ROLES = List.of("PARENT", "STAFF", "ADMIN");

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String role = resolveRole(request.role());
        String hashedPassword = passwordEncoder.encode(request.password());
        String createdAt = LocalDateTime.now().toString();

        UserEntity user = new UserEntity(
            request.fullName().trim(),
            request.email().toLowerCase().trim(),
            hashedPassword,
            role,
            request.phone() != null ? request.phone().trim() : null,
            createdAt
        );

        UserEntity saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new InvalidCredentialsException();
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidCredentialsException();
        }

        UserEntity user = userRepository.findByEmail(request.email().toLowerCase().trim())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toResponse(user);
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (request.email() == null || !request.email().contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }

    private String resolveRole(String rawRole) {
        if (rawRole == null) return "PARENT";
        String upper = rawRole.toUpperCase().trim();
        return VALID_ROLES.contains(upper) ? upper : "PARENT";
    }

    private AuthResponse toResponse(UserEntity user) {
        return new AuthResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getPhone() != null ? user.getPhone() : "",
            user.getCreatedAt() != null ? user.getCreatedAt() : ""
        );
    }
}
