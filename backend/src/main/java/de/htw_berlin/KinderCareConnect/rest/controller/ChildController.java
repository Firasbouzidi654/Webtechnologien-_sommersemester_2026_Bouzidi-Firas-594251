package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.HealthPrototypeService;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final HealthPrototypeService healthPrototypeService;

    public ChildController(HealthPrototypeService healthPrototypeService) {
        this.healthPrototypeService = healthPrototypeService;
    }

    @GetMapping
    public List<ChildHealthResponse> getAllChildren() {
        return healthPrototypeService.getAllChildren();
    }

    @GetMapping("/{id}")
    public ChildHealthResponse getChildById(@PathVariable Long id) {
        return healthPrototypeService.getChildById(id);
    }
}
