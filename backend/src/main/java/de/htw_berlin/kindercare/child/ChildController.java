package de.htw_berlin.kindercare.child;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/children")
public class ChildController {
    private final ChildService service;

    public ChildController(ChildService service) { this.service = service; }

    @GetMapping
    public List<Child> getAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Child create(@RequestBody Child child) {
        if (child.getName() == null || child.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child name is required.");
        }
        return service.create(child);
    }
}
