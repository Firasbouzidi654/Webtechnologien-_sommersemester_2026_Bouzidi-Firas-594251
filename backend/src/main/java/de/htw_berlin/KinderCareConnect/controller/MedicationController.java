package de.htw_berlin.KinderCareConnect.controller;

import de.htw_berlin.KinderCareConnect.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationTaskResponse;
import de.htw_berlin.KinderCareConnect.service.MedicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationResponse> getAllMedications() {
        return medicationService.getAllMedications();
    }

    @GetMapping("/child/{childId}")
    public List<MedicationResponse> getMedicationsByChild(@PathVariable Long childId) {
        return medicationService.getMedicationsByChild(childId);
    }

    @PostMapping("/child/{childId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationResponse createMedication(
        @PathVariable Long childId,
        @RequestBody Map<String, Object> data
    ) {
        return medicationService.createMedication(childId, data);
    }

    @PutMapping("/{medicationId}")
    public MedicationResponse updateMedication(
        @PathVariable String medicationId,
        @RequestBody Map<String, Object> data
    ) {
        return medicationService.updateMedication(medicationId, data);
    }

    @PutMapping("/{medicationId}/taken")
    public MedicationTaskResponse markMedicationTaken(@PathVariable String medicationId) {
        return medicationService.markMedicationTaken(medicationId);
    }

    @PutMapping("/{medicationId}/status")
    public MedicationResponse updateMedicationStatus(
        @PathVariable String medicationId,
        @RequestBody Map<String, String> body
    ) {
        return medicationService.updateMedicationStatus(medicationId, body.getOrDefault("status", "Pending"));
    }

    @DeleteMapping("/{medicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedication(@PathVariable String medicationId) {
        medicationService.deleteMedication(medicationId);
    }
}
