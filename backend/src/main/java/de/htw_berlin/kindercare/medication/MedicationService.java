package de.htw_berlin.kindercare.medication;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository repository;

    public MedicationService(MedicationRepository repository) {
        this.repository = repository;
    }

    public List<Medication> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public Medication create(Medication medication) {
        return repository.save(new Medication(
                medication.getName().trim(),
                medication.getChildName().trim(),
                medication.getDosage(),
                medication.getTime() == null ? "12:00" : medication.getTime(),
                medication.getStatus() == null ? "PENDING" : medication.getStatus()
        ));
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

    public void renameChild(String oldName, String newName) {
        repository.findByChildName(oldName).forEach(medication -> {
            medication.setChildName(newName);
            repository.save(medication);
        });
    }

    public void deleteByChildName(String childName) {
        repository.deleteAll(repository.findByChildName(childName));
    }

    private Medication findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found."));
    }
}
