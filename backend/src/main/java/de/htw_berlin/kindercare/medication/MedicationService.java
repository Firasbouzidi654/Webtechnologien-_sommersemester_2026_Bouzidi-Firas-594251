package de.htw_berlin.kindercare.medication;

import de.htw_berlin.kindercare.child.Child;
import de.htw_berlin.kindercare.child.ChildRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final ChildRepository children;

    public MedicationService(MedicationRepository repository, ChildRepository children) {
        this.repository = repository;
        this.children = children;
    }

    @NonNull
    public List<Medication> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @NonNull
    public Medication create(@NonNull Medication medication) {
        Long childId = medication.getChildId();
        if (childId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A child is required.");
        }

        Child linkedChild = children.findById(childId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."));

        Medication toSave = new Medication(
                medication.getName().trim(),
                linkedChild.getName(),
                medication.getDosage(),
                medication.getTime() == null ? "12:00" : medication.getTime(),
                medication.getStatus() == null ? "PENDING" : medication.getStatus(),
                medication.getFrequency() == null ? "DAILY" : medication.getFrequency(),
                medication.getIntervalDays(),
                medication.getStartDate()
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
        if (changes.getFrequency() != null) medication.setFrequency(changes.getFrequency());
        if (changes.getIntervalDays() != null) medication.setIntervalDays(changes.getIntervalDays());
        if (changes.getStartDate() != null) medication.setStartDate(changes.getStartDate());

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
