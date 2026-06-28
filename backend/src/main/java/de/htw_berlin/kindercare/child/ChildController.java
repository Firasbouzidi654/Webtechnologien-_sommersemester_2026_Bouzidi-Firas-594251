package de.htw_berlin.kindercare.child;

import de.htw_berlin.kindercare.config.RoleAccess;
import de.htw_berlin.kindercare.medication.MedicationService;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/children")
public class ChildController {
    private final ChildService service;
    private final MedicationService medicationService;

    public ChildController(ChildService service, MedicationService medicationService) {
        this.service = service;
        this.medicationService = medicationService;
    }

    @GetMapping
    @NonNull
    public List<Child> getAll(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId
    ) {
        // Access rule: admins/staff receive the full directory; parents only receive
        // children whose parent_id matches their logged-in account id.
        return service.findVisibleTo(role, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NonNull
    public Child create(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId,
            @RequestBody @NonNull Child child
    ) {
        if (child.getName() == null || child.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child name is required.");
        }
        validateAllergies(child);
        return service.create(child, role, userId);
    }

    @PutMapping("/{id}")
    @NonNull
    public Child update(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId,
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Child child
    ) {
        validateAllergies(child);
        Child updated = service.update(id, child, role, userId);
        medicationService.updateChildName(id, updated.getName());
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestHeader(value = "X-User-Id", required = false) @Nullable String userId,
            @PathVariable @NonNull Long id
    ) {
        RoleAccess.require(role, "PARENT", "ADMIN");
        service.findAccessibleById(id, role, userId);
        medicationService.deleteByChildId(id);
        service.delete(id, role, userId);
    }

    private void validateAllergies(Child child) {
        String allergies = child.getAllergies();
        if (allergies == null || allergies.isBlank()) {
            return;
        }

        boolean hasEmptyEntry = Arrays.stream(allergies.split(",", -1)).anyMatch(entry -> entry.trim().isEmpty());
        if (hasEmptyEntry) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allergy entries cannot be blank.");
        }
    }
}
