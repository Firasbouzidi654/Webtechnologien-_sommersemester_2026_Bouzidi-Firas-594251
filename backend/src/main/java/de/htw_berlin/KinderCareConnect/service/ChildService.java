package de.htw_berlin.KinderCareConnect.service;

import de.htw_berlin.KinderCareConnect.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.entity.EmergencyContactEntity;
import de.htw_berlin.KinderCareConnect.entity.MedicationEntity;
import de.htw_berlin.KinderCareConnect.exception.ResourceNotFoundException;
import de.htw_berlin.KinderCareConnect.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.model.EmergencyContactResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationLogResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationScheduleResponse;
import de.htw_berlin.KinderCareConnect.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.repository.EmergencyContactRepository;
import de.htw_berlin.KinderCareConnect.repository.MedicationLogRepository;
import de.htw_berlin.KinderCareConnect.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final MedicationRepository medicationRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final MedicationLogRepository medicationLogRepository;

    public ChildService(
        ChildRepository childRepository,
        MedicationRepository medicationRepository,
        EmergencyContactRepository emergencyContactRepository,
        MedicationLogRepository medicationLogRepository
    ) {
        this.childRepository = childRepository;
        this.medicationRepository = medicationRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.medicationLogRepository = medicationLogRepository;
    }

    public List<ChildHealthResponse> getAllChildren() {
        return childRepository.findAllByOrderByIdAsc()
            .stream()
            .map(this::toHealthResponse)
            .toList();
    }

    public ChildHealthResponse getChildById(Long id) {
        ChildEntity child = childRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + id));
        return toHealthResponse(child);
    }

    public ChildHealthResponse createChild(ChildEntity child) {
        if (child.getName() == null || child.getName().isBlank()) {
            throw new IllegalArgumentException("Child name is required");
        }
        if (child.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (child.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        return toHealthResponse(childRepository.save(child));
    }

    @Transactional
    public ChildHealthResponse updateChild(Long id, Map<String, Object> fields) {
        ChildEntity child = childRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + id));

        if (fields.containsKey("name") && fields.get("name") != null) {
            child.setName((String) fields.get("name"));
        }
        if (fields.containsKey("groupName") && fields.get("groupName") != null) {
            child.setGroupName((String) fields.get("groupName"));
        }
        if (fields.containsKey("parentName") && fields.get("parentName") != null) {
            child.setParentName((String) fields.get("parentName"));
        }
        if (fields.containsKey("parentEmail") && fields.get("parentEmail") != null) {
            child.setParentEmail((String) fields.get("parentEmail"));
        }
        if (fields.containsKey("healthNotes")) {
            child.setHealthNotes((String) fields.get("healthNotes"));
        }
        if (fields.containsKey("allergies")) {
            Object val = fields.get("allergies");
            if (val instanceof List<?> list) {
                child.setAllergies(String.join(",", list.stream().map(Object::toString).toList()));
            }
        }
        if (fields.containsKey("chronicDiseases")) {
            Object val = fields.get("chronicDiseases");
            if (val instanceof List<?> list) {
                child.setChronicDiseases(String.join(",", list.stream().map(Object::toString).toList()));
            }
        }

        return toHealthResponse(childRepository.save(child));
    }

    @Transactional
    public void deleteChild(Long id) {
        if (!childRepository.existsById(id)) {
            throw new ResourceNotFoundException("Child not found: " + id);
        }
        childRepository.deleteById(id);
    }

    public EmergencyContactResponse addEmergencyContact(Long childId, Map<String, Object> data) {
        childRepository.findById(childId)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        EmergencyContactEntity contact = new EmergencyContactEntity(
            childId,
            (String) data.get("name"),
            (String) data.getOrDefault("relationship", ""),
            (String) data.getOrDefault("phone", ""),
            (String) data.getOrDefault("email", ""),
            data.get("priority") instanceof Number n ? n.intValue() : 1
        );

        return toContactResponse(emergencyContactRepository.save(contact));
    }

    @Transactional
    public EmergencyContactResponse updateEmergencyContact(Long contactId, Map<String, Object> data) {
        EmergencyContactEntity contact = emergencyContactRepository.findById(contactId)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found: " + contactId));

        if (data.containsKey("name")) contact.setName((String) data.get("name"));
        if (data.containsKey("relationship")) contact.setRelationship((String) data.get("relationship"));
        if (data.containsKey("phone")) contact.setPhone((String) data.get("phone"));
        if (data.containsKey("email")) contact.setEmail((String) data.get("email"));
        if (data.containsKey("priority")) {
            Object p = data.get("priority");
            contact.setPriority(p instanceof Number n ? n.intValue() : 1);
        }

        return toContactResponse(emergencyContactRepository.save(contact));
    }

    @Transactional
    public void deleteEmergencyContact(Long contactId) {
        emergencyContactRepository.deleteById(contactId);
    }

    // ─── Mapping helpers ────────────────────────────────────────────────────────

    public ChildHealthResponse toHealthResponse(ChildEntity child) {
        List<MedicationResponse> meds = medicationRepository
            .findByChildIdOrderById(child.getId())
            .stream()
            .map(med -> toMedicationResponse(med, child.getName()))
            .toList();

        List<EmergencyContactResponse> contacts = emergencyContactRepository
            .findByChildIdOrderByPriority(child.getId())
            .stream()
            .map(this::toContactResponse)
            .toList();

        return new ChildHealthResponse(
            child.getId(),
            child.getName(),
            child.getGroupName() != null ? child.getGroupName() : "General",
            child.getDateOfBirth() != null ? child.getDateOfBirth().toString() : null,
            child.getParentName() != null ? child.getParentName() : "Parent",
            child.getParentEmail() != null ? child.getParentEmail() : "",
            splitToList(child.getAllergies()),
            splitToList(child.getChronicDiseases()),
            child.getHealthNotes() != null ? child.getHealthNotes() : "",
            meds,
            contacts
        );
    }

    private MedicationResponse toMedicationResponse(MedicationEntity med, String childName) {
        List<MedicationLogResponse> history = medicationLogRepository
            .findByMedicationIdOrderByLoggedAtDesc(med.getMedicationId())
            .stream()
            .map(log -> new MedicationLogResponse(
                log.getId(),
                log.getMedicationId(),
                log.getChildId(),
                log.getStatus(),
                log.getAdminName() != null ? log.getAdminName() : "",
                log.getLoggedAt() != null ? log.getLoggedAt() : "",
                log.getNote() != null ? log.getNote() : ""
            ))
            .toList();

        MedicationScheduleResponse schedule = new MedicationScheduleResponse(
            med.getFrequency() != null ? med.getFrequency() : "Daily",
            med.getDayPart() != null ? med.getDayPart() : "Morning",
            med.getScheduledTime() != null ? med.getScheduledTime() : "12:00"
        );

        return new MedicationResponse(
            med.getId(),
            med.getMedicationId(),
            med.getChildId(),
            childName,
            med.getName() != null ? med.getName() : "",
            med.getActiveIngredient() != null ? med.getActiveIngredient() : "",
            med.getDosage() != null ? med.getDosage() : "",
            med.getInstructions() != null ? med.getInstructions() : "",
            schedule,
            history,
            med.isPrescriptionUploaded(),
            med.getTodayStatus() != null ? med.getTodayStatus() : "Pending"
        );
    }

    private EmergencyContactResponse toContactResponse(EmergencyContactEntity c) {
        return new EmergencyContactResponse(
            c.getId(),
            c.getName(),
            c.getRelationship() != null ? c.getRelationship() : "",
            c.getPhone() != null ? c.getPhone() : "",
            c.getEmail() != null ? c.getEmail() : "",
            c.getPriority()
        );
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    }
}
