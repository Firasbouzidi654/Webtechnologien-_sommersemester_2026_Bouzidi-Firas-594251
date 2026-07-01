package de.htw_berlin.kindercare.medication;

import de.htw_berlin.kindercare.child.Child;
import de.htw_berlin.kindercare.child.ChildRepository;
import de.htw_berlin.kindercare.child.ChildService;
import de.htw_berlin.kindercare.config.RoleAccess;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final ChildRepository children;
    private final ChildService childService;

    public MedicationService(MedicationRepository repository, ChildRepository children, ChildService childService) {
        this.repository = repository;
        this.children = children;
        this.childService = childService;
    }

    @NonNull
    public List<Medication> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @NonNull
    public List<Medication> findVisibleTo(@Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "STAFF", "ADMIN");
        if (RoleAccess.hasAny(role, "STAFF", "ADMIN")) {
            return findAll();
        }

        List<Long> childIds = new ArrayList<>();
        for (Child child : childService.findVisibleTo(role, userId)) {
            Long childId = child.getId();
            if (childId != null) {
                childIds.add(childId);
            }
        }
        if (childIds.isEmpty()) {
            return List.of();
        }
        return repository.findByChildIdInOrderByIdAsc(childIds);
    }

    @NonNull
    public Medication create(@NonNull Medication medication, @Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "STAFF", "ADMIN");
        Long childId = medication.getChildId();
        if (childId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child is required.");
        }

        // Medication records expose child details, so parent-created medication must
        // be linked only to a child assigned to that same parent account.
        Child linkedChild = RoleAccess.hasAny(role, "STAFF", "ADMIN")
                ? children.findById(childId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."))
                : childService.findAccessibleById(childId, role, userId);

        Medication toSave = new Medication(
                medication.getName().trim(),
                linkedChild.getName(),
                medication.getDosage(),
                medication.getTime() == null ? "12:00" : medication.getTime(),
                medication.getStatus() == null ? "PENDING" : medication.getStatus(),
                medication.getScheduledDate() == null ? LocalDate.now().toString() : medication.getScheduledDate()
        );

        toSave.setChildId(linkedChild.getId());

        return repository.save(toSave);
    }

    @NonNull
    public Medication update(@NonNull Long id, @NonNull Medication changes) {
        Medication medication = findById(id);

        if (changes.getName() != null && !changes.getName().isBlank()) medication.setName(changes.getName().trim());
        if (changes.getDosage() != null) medication.setDosage(changes.getDosage());
        if (changes.getTime() != null) medication.setTime(changes.getTime());
        if (changes.getStatus() != null) medication.setStatus(changes.getStatus());
        if (changes.getScheduledDate() != null) medication.setScheduledDate(changes.getScheduledDate());

        return repository.save(medication);
    }

    public void delete(@NonNull Long id) {
        repository.delete(findById(id));
    }

    // Keep the existing display field in sync; medication ownership uses childId only.
    public void updateChildName(@NonNull Long childId, @NonNull String newName) {
        repository.findByChildId(childId).forEach(medication -> {
            medication.setChildName(newName);
            repository.save(medication);
        });
    }

    public void deleteByChildId(@NonNull Long childId) {
        repository.deleteAll(repository.findByChildId(childId));
    }

    @NonNull
    private Medication findById(@NonNull Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found."));
    }
}
