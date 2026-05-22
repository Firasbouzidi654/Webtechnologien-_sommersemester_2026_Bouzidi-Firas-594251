package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.mapping.ChildRestMapper;
import de.htw_berlin.KinderCareConnect.business.service.HealthPrototypeService;
import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.rest.model.ChildResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final HealthPrototypeService healthPrototypeService;
    private final ChildRepository childRepository;
    private final ChildRestMapper childRestMapper;

    public ChildController(
        HealthPrototypeService healthPrototypeService,
        ChildRepository childRepository,
        ChildRestMapper childRestMapper
    ) {
        this.healthPrototypeService = healthPrototypeService;
        this.childRepository = childRepository;
        this.childRestMapper = childRestMapper;
    }

    @GetMapping
    public List<ChildHealthResponse> getAllChildren() {
        return healthPrototypeService.getAllChildren();
    }

    @GetMapping("/{id}")
    public ChildHealthResponse getChildById(@PathVariable Long id) {
        return healthPrototypeService.getChildById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChildResponse createChild(@RequestBody ChildEntity child) {
        ChildEntity savedChild = childRepository.save(child);
        return childRestMapper.toResponse(savedChild);
    }
}
