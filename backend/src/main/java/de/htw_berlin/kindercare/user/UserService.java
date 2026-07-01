package de.htw_berlin.kindercare.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class UserService {
    private static final Set<String> REGISTERABLE_ROLES = Set.of("PARENT", "STAFF");
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public User register(@Nullable String email, @Nullable String password, @Nullable String role) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (repository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        String normalizedRole = (role == null || role.isBlank()) ? "PARENT" : role.trim().toUpperCase();
        if (!REGISTERABLE_ROLES.contains(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be PARENT or STAFF.");
        }
        User user = new User(normalizedEmail, passwordEncoder.encode(password), normalizedRole);
        return repository.save(user);
    }

    @NonNull
    public User login(@Nullable String email, @Nullable String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (password == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return repository.findByEmail(normalizedEmail)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
    }
}
