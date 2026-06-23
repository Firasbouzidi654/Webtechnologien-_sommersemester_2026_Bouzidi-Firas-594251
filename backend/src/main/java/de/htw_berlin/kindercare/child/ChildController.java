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
    public List<Child> getAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @NonNull
    public Child create(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @RequestBody @NonNull Child child
    ) {
        RoleAccess.require(role, "PARENT");
        if (child.getName() == null || child.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child name is required.");
        }
        return service.create(child);
    }

    @PutMapping("/{id}")
    @NonNull
    public Child update(
            @RequestHeader(value = "X-User-Role", required = false) @Nullable String role,
            @PathVariable @NonNull Long id,
            @RequestBody @NonNull Child child
    ) {
        RoleAccess.require(role, "PARENT");
        Child updated = service.update(id, child);
        medicationService.updateChildName(id, updated.getName());
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Role", required = false) @Nullable String role, @PathVariable @NonNull Long id) {
        RoleAccess.require(role, "PARENT");
        medicationService.deleteByChildId(id);
        service.delete(id);
    }
}
