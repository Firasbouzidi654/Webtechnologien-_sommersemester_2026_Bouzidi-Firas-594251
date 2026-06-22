package de.htw_berlin.kindercare.medication;

import de.htw_berlin.kindercare.child.Child;
import de.htw_berlin.kindercare.child.ChildRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    private final ChildRepository children;

    public MedicationService(MedicationRepository repository, ChildRepository children) {
        this.repository = repository;
        this.children = children;
    }

    public List<Medication> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public Medication create(Medication medication) {
        Medication toSave = new Medication(
                medication.getName().trim(),
                medication.getChildName().trim(),
                medication.getDosage(),
                medication.getTime() == null ? "12:00" : medication.getTime(),
                medication.getStatus() == null ? "PENDING" : medication.getStatus()
        );

        // When the frontend sends a real child id, trust the database record over the
        // typed child name so medication always links to the correct child, even if two
        // children share the same name.
        if (medication.getChildId() != null) {
            Child child = children.findById(medication.getChildId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."));
            toSave.setChildId(child.getId());
            toSave.setChildName(child.getName());
        }

        return repository.save(toSave);
    }

    public Medication update(Long id, Medication changes) {
        Medication medication = findById(id);

        if (changes.getName() != null && !changes.getName().isBlank()) medication.setName(changes.getName().trim());
        if (changes.getChildName() != null && !changes.getChildName().isBlank()) medication.setChildName(changes.getChildName().trim());
        if (changes.getDosage() != null) medication.setDosage(changes.getDosage());
        if (changes.getTime() != null) medication.setTime(changes.getTime());
        if (changes.getStatus() != null) medication.setStatus(changes.getStatus());

        return repository.save(medication);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    // Renaming/deleting by child id is precise even if two children share the same name.
    // Legacy medications saved before child ids existed only match by the old name.
    public void renameChild(Long childId, String oldName, String newName) {
        medicationsForChild(childId, oldName).forEach(medication -> {
            medication.setChildName(newName);
            repository.save(medication);
        });
    }

    public void deleteByChildName(Long childId, String childName) {
        repository.deleteAll(medicationsForChild(childId, childName));
    }

    private List<Medication> medicationsForChild(Long childId, String childName) {
        List<Medication> matches = new ArrayList<>();
        if (childId != null) {
            matches.addAll(repository.findByChildId(childId));
        }
        repository.findByChildName(childName).stream()
                .filter(medication -> medication.getChildId() == null)
                .forEach(matches::add);
        return matches;
    }

    private Medication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found."));
    }
}
