package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.HealthPrototypeService;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final HealthPrototypeService healthPrototypeService;

    public MedicationController(HealthPrototypeService healthPrototypeService) {
        this.healthPrototypeService = healthPrototypeService;
    }

    @GetMapping
    public List<MedicationResponse> getAllMedications() {
        return healthPrototypeService.getAllMedications();
    }

    @PutMapping("/{medicationId}/taken")
    public MedicationTaskResponse markMedicationTaken(@PathVariable String medicationId) {
        return healthPrototypeService.markMedicationTaken(medicationId);
    }
}
