package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.ChildService;
import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.rest.model.EmergencyContactResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final ChildService childService;

    public ChildController(ChildService childService) {
        this.childService = childService;
    }

    @GetMapping
    public List<ChildHealthResponse> getAllChildren() {
        return childService.getAllChildren();
    }

    @GetMapping("/{id}")
    public ChildHealthResponse getChildById(@PathVariable Long id) {
        return childService.getChildById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChildHealthResponse createChild(@RequestBody ChildEntity child) {
        return childService.createChild(child);
    }

    @PutMapping("/{id}")
    public ChildHealthResponse updateChild(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return childService.updateChild(id, fields);
    }

    @PatchMapping("/{id}")
    public ChildHealthResponse patchChild(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return childService.updateChild(id, fields);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChild(@PathVariable Long id) {
        childService.deleteChild(id);
    }

    // ─── Emergency contacts ─────────────────────────────────────────────────

    @PostMapping("/{childId}/emergency-contacts")
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyContactResponse addEmergencyContact(
        @PathVariable Long childId,
        @RequestBody Map<String, Object> data
    ) {
        return childService.addEmergencyContact(childId, data);
    }

    @PutMapping("/emergency-contacts/{contactId}")
    public EmergencyContactResponse updateEmergencyContact(
        @PathVariable Long contactId,
        @RequestBody Map<String, Object> data
    ) {
        return childService.updateEmergencyContact(contactId, data);
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmergencyContact(@PathVariable Long contactId) {
        childService.deleteEmergencyContact(contactId);
    }

    // ─── Parent notes ────────────────────────────────────────────────────────

    @PostMapping("/{childId}/notes")
    public Map<String, String> saveParentNote(
        @PathVariable Long childId,
        @RequestBody Map<String, String> body
    ) {
        return childService.saveParentNote(childId, body.getOrDefault("note", ""));
    }

    @GetMapping("/{childId}/notes")
    public Map<String, String> getParentNote(@PathVariable Long childId) {
        Optional<String> note = childService.getParentNote(childId);
        return note.map(n -> Map.of("note", n)).orElse(Map.of("note", ""));
    }
}
