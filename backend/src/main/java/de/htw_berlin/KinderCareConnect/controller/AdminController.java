package de.htw_berlin.KinderCareConnect.controller;

import de.htw_berlin.KinderCareConnect.model.DailyStatsResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationTaskResponse;
import de.htw_berlin.KinderCareConnect.service.MedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MedicationService medicationService;

    public AdminController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/tasks/today")
    public List<MedicationTaskResponse> getTodayMedicationTasks() {
        return medicationService.getTodayTasks();
    }

    @GetMapping("/stats/today")
    public DailyStatsResponse getTodayStats() {
        return medicationService.getTodayStats();
    }
}
