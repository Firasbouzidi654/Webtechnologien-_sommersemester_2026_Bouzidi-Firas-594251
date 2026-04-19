package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.ChildService;
import de.htw_berlin.KinderCareConnect.rest.model.ChildResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @GetMapping
    public List<ChildResponse> getAllChildren() {
        return childService.getAllChildren();
    }
}
