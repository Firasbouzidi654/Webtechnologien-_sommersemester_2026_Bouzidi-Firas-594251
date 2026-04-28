package de.htw_berlin.KinderCareConnect.business.service;

import de.htw_berlin.KinderCareConnect.persistence.repository.HealthPrototypeRepository;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.rest.model.DailyStatsResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationTaskResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthPrototypeService {

    private final HealthPrototypeRepository healthPrototypeRepository;

    public HealthPrototypeService(HealthPrototypeRepository healthPrototypeRepository) {
        this.healthPrototypeRepository = healthPrototypeRepository;
    }

    public List<ChildHealthResponse> getAllChildren() {
        return healthPrototypeRepository.findAllChildren();
    }

    public ChildHealthResponse getChildById(Long id) {
        return healthPrototypeRepository.findChildById(id);
    }

    public List<MedicationResponse> getAllMedications() {
        return healthPrototypeRepository.findAllMedications();
    }

    public List<MedicationTaskResponse> getTodayMedicationTasks() {
        return healthPrototypeRepository.findTodayMedicationTasks();
    }

    public MedicationTaskResponse markMedicationTaken(String medicationId) {
        return healthPrototypeRepository.markMedicationTaken(medicationId);
    }

    public DailyStatsResponse getTodayStats() {
        return healthPrototypeRepository.getTodayStats();
    }
}
