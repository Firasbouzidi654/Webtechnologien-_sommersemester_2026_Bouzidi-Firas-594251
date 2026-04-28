package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.HealthPrototypeService;
import de.htw_berlin.KinderCareConnect.rest.model.DailyStatsResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final HealthPrototypeService healthPrototypeService;

    public AdminController(HealthPrototypeService healthPrototypeService) {
        this.healthPrototypeService = healthPrototypeService;
    }

    @GetMapping("/tasks/today")
    public List<MedicationTaskResponse> getTodayMedicationTasks() {
        return healthPrototypeService.getTodayMedicationTasks();
    }

    @GetMapping("/stats/today")
    public DailyStatsResponse getTodayStats() {
        return healthPrototypeService.getTodayStats();
    }
}
