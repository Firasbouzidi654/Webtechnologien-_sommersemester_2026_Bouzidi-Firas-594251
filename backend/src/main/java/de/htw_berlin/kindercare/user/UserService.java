package de.htw_berlin.kindercare.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User register(String email, String password, String role) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (repository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        String normalizedRole = (role == null || role.isBlank()) ? "PARENT" : role.trim().toUpperCase();
        User user = new User(normalizedEmail, passwordEncoder.encode(password), normalizedRole);
        return repository.save(user);
    }

    public User login(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        User user = repository.findByEmail(normalizedEmail).orElse(null);

        if (user == null || password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return user;
    }
}
