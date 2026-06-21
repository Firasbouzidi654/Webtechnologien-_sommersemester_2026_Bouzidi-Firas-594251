package de.htw_berlin.kindercare.staff;

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
@RequestMapping("/api/staff")
public class StaffController {
    private final StaffService service;

    public StaffController(StaffService service) { this.service = service; }

    @GetMapping
    public List<Staff> getAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Staff create(@RequestBody Staff staff) {
        if (staff.getName() == null || staff.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A staff name is required.");
        }
        return service.create(staff);
    }
}
