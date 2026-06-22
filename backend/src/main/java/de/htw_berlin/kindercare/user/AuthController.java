package de.htw_berlin.kindercare.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody User request) {
        return service.register(request.getEmail(), request.getPassword(), request.getRole());
    }

    @PostMapping("/login")
    public User login(@RequestBody User request) {
        return service.login(request.getEmail(), request.getPassword());
    }
}
