package de.htw_berlin.kindercare.medication;

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
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService service;

    public MedicationController(MedicationService service) { this.service = service; }

    @GetMapping
    public List<Medication> getAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medication create(@RequestBody Medication medication) {
        if (medication.getName() == null || medication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication name is required.");
        }
        return service.create(medication);
    }
}
