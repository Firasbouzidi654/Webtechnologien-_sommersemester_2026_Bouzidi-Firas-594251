package de.htw_berlin.kindercare.medication;

import de.htw_berlin.kindercare.config.RoleAccess;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private static final Set<String> STATUSES = Set.of("UPCOMING", "PENDING", "TAKEN", "MISSED");
    private final MedicationService service;

    public MedicationController(MedicationService service) { this.service = service; }

    @GetMapping
    @NonNull
    public List<Medication> getAll(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId
    ) {
        return service.findVisibleTo(role, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NonNull
    public Medication create(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId,
            @RequestBody @NonNull Medication medication
    ) {
        requireMedicationDetails(medication);
        normalizeStatus(medication);
        normalizeScheduledDate(medication);
        return service.create(medication, role, userId);
    }

    @PutMapping("/{id}")
    @NonNull
    public Medication update(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Medication medication
    ) {
        RoleAccess.require(role, "STAFF", "ADMIN");
        requireNonBlankUpdateFields(medication);
        if (medication.getTime() != null && !medication.getTime().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must use the HH:mm format.");
        }
        normalizeStatus(medication);
        normalizeScheduledDate(medication);
        return service.update(id, medication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Role", required = false) @Nullable String role, @PathVariable @NonNull Long id) {
        RoleAccess.require(role, "STAFF", "ADMIN");
        service.delete(id);
    }

    private void requireMedicationDetails(Medication medication) {
        if (medication.getName() == null || medication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication name is required.");
        }
        if (medication.getDosage() == null || medication.getDosage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication dosage is required.");
        }
        if (medication.getChildId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child is required.");
        }
        if (medication.getTime() != null && !medication.getTime().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Time must use the HH:mm format.");
        }
        if (medication.getScheduledDate() == null || medication.getScheduledDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A scheduled date is required.");
        }
    }

    private void requireNonBlankUpdateFields(Medication medication) {
        if (medication.getName() != null && medication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication name cannot be blank.");
        }
        if (medication.getDosage() != null && medication.getDosage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication dosage cannot be blank.");
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

    private void normalizeScheduledDate(Medication medication) {
        if (medication.getScheduledDate() != null && !medication.getScheduledDate().isBlank()) {
            try {
                medication.setScheduledDate(LocalDate.parse(medication.getScheduledDate()).toString());
            } catch (DateTimeParseException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled date must use the YYYY-MM-DD format.");
            }
        }
    }
}
