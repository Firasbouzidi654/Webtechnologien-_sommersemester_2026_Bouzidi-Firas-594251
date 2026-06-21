package de.htw_berlin.kindercare.medication;

import org.springframework.stereotype.Service;

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
                medication.getChildName(),
                medication.getDosage()
        ));
    }
}
