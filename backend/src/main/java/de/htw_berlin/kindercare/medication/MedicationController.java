package de.htw_berlin.kindercare.medication;

import de.htw_berlin.kindercare.config.RoleAccess;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private static final Set<String> STATUSES = Set.of("UPCOMING", "PENDING", "TAKEN", "MISSED");
    private final MedicationService service;

    public MedicationController(MedicationService service) { this.service = service; }

    @GetMapping
    public List<Medication> getAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medication create(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody Medication medication
    ) {
        requireMedicationDetails(medication);
        RoleAccess.require(role, "PARENT", "STAFF", "ADMIN");
        normalizeStatus(medication);
        return service.create(medication);
    }

    @PutMapping("/{id}")
    public Medication update(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody Medication medication
    ) {
        RoleAccess.require(role, "STAFF", "ADMIN");
        if (medication.getTime() != null && !medication.getTime().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must use the HH:mm format.");
        }
        normalizeStatus(medication);
        return service.update(id, medication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Role", required = false) String role, @PathVariable Long id) {
        RoleAccess.require(role, "STAFF", "ADMIN");
        service.delete(id);
    }

    private void requireMedicationDetails(Medication medication) {
        if (medication.getName() == null || medication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication name is required.");
        }
        if (medication.getChildName() == null || medication.getChildName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child name is required.");
        }
        if (medication.getTime() != null && !medication.getTime().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must use the HH:mm format.");
        }
    }

    private void normalizeStatus(Medication medication) {
        if (medication.getStatus() == null) return;

        String status = medication.getStatus().trim().toUpperCase();
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be UPCOMING, PENDING, TAKEN, or MISSED.");
        }
        medication.setStatus(status);
    }
}
