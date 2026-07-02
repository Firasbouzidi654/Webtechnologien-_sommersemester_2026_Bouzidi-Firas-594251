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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final ChildRepository children;
    private final ChildService childService;
    private final Clock clock;

    public MedicationService(MedicationRepository repository, ChildRepository children, ChildService childService, Clock clock) {
        this.repository = repository;
        this.children = children;
        this.childService = childService;
        this.clock = clock;
    }

    @NonNull
    public List<Medication> findAll() {
        return resolveOverdueStatuses(repository.findAllByOrderByIdAsc());
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
        return resolveOverdueStatuses(repository.findByChildIdInOrderByIdAsc(childIds));
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
                medication.getScheduledDate() == null ? LocalDate.now(clock).toString() : medication.getScheduledDate()
        );

        toSave.setChildId(linkedChild.getId());

        markMissedIfOverdue(toSave);
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

        markMissedIfOverdue(medication);
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
        Medication medication = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found."));
        return saveIfStatusChanged(medication);
    }

    @NonNull
    private List<Medication> resolveOverdueStatuses(@NonNull List<Medication> medications) {
        return medications.stream().map(this::saveIfStatusChanged).toList();
    }

    @NonNull
    private Medication saveIfStatusChanged(@NonNull Medication medication) {
        String previousStatus = medication.getStatus();
        markMissedIfOverdue(medication);
        if (!String.valueOf(previousStatus).equals(String.valueOf(medication.getStatus()))) {
            return repository.save(medication);
        }
        return medication;
    }

    private void markMissedIfOverdue(@NonNull Medication medication) {
        if (!"PENDING".equalsIgnoreCase(medication.getStatus())) {
            return;
        }

        try {
            LocalDate scheduledDate = LocalDate.parse(medication.getScheduledDate());
            LocalTime scheduledTime = LocalTime.parse(medication.getTime() == null ? "12:00" : medication.getTime());
            if (LocalDateTime.of(scheduledDate, scheduledTime).isBefore(LocalDateTime.now(clock))) {
                medication.setStatus("MISSED");
            }
        } catch (DateTimeParseException | NullPointerException ignored) {
            // Invalid dates/times are rejected by the controller for new writes.
        }
    }
}
