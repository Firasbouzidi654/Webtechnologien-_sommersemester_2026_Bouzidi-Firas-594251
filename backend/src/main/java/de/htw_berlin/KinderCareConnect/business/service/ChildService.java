package de.htw_berlin.KinderCareConnect.business.service;

import de.htw_berlin.KinderCareConnect.business.error.ResourceNotFoundException;
import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.persistence.entity.EmergencyContactEntity;
import de.htw_berlin.KinderCareConnect.persistence.entity.ParentNoteEntity;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.EmergencyContactRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.MedicationLogRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.MedicationRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.ParentNoteRepository;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.rest.model.EmergencyContactResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationLogResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationScheduleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final MedicationRepository medicationRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final ParentNoteRepository parentNoteRepository;

    public ChildService(
        ChildRepository childRepository,
        MedicationRepository medicationRepository,
        EmergencyContactRepository emergencyContactRepository,
        MedicationLogRepository medicationLogRepository,
        ParentNoteRepository parentNoteRepository
    ) {
        this.childRepository = childRepository;
        this.medicationRepository = medicationRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.parentNoteRepository = parentNoteRepository;
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
        validatePhotoUrl(child.getPhotoUrl());
        ChildEntity saved = childRepository.save(child);
        return toHealthResponse(saved);
    }

    private void validatePhotoUrl(String url) {
        if (url == null || url.isBlank()) return;
        if (url.startsWith("data:")) {
            throw new IllegalArgumentException(
                "Photo must be an https:// URL, not a Base64 image. " +
                "Upload your image to an image host and paste the link instead.");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException(
                "Photo URL must start with http:// or https://");
        }
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
        if (fields.containsKey("photoUrl")) {
            String url = (String) fields.get("photoUrl");
            validatePhotoUrl(url);
            child.setPhotoUrl(url);
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

        EmergencyContactEntity saved = emergencyContactRepository.save(contact);
        return toContactResponse(saved);
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

    public Map<String, String> saveParentNote(Long childId, String note) {
        childRepository.findById(childId)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        Optional<ParentNoteEntity> existing = parentNoteRepository.findByChildId(childId);
        ParentNoteEntity entity = existing.orElseGet(() -> new ParentNoteEntity(childId, note));
        entity.setNote(note);
        parentNoteRepository.save(entity);
        return Map.of("status", "saved", "childId", String.valueOf(childId));
    }

    public Optional<String> getParentNote(Long childId) {
        return parentNoteRepository.findByChildId(childId).map(ParentNoteEntity::getNote);
    }

    // ─── mapping helpers ────────────────────────────────────────────────────

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
            contacts,
            child.getPhotoUrl()
        );
    }

    private MedicationResponse toMedicationResponse(
        de.htw_berlin.KinderCareConnect.persistence.entity.MedicationEntity med,
        String childName
    ) {
        List<MedicationLogResponse> history = medicationLogRepository
            .findByMedicationIdOrderByLoggedAtDesc(med.getMedicationId())
            .stream()
            .map(log -> new MedicationLogResponse(
                log.getId(),
                log.getMedicationId(),
                log.getChildId(),
                log.getStatus(),
                log.getAdminName(),
                log.getLoggedAt() != null ? log.getLoggedAt() : "",
                log.getNote() != null ? log.getNote() : ""
            ))
            .toList();

        MedicationScheduleResponse schedule = new MedicationScheduleResponse(
            med.getId() != null ? med.getId() + 1000L : 0L,
            med.getFrequency() != null ? med.getFrequency() : "Daily",
            med.getDayPart() != null ? med.getDayPart() : "Morning",
            med.getScheduledTime() != null ? med.getScheduledTime() : "12:00",
            med.getDosage() != null ? med.getDosage() : "",
            med.getInstructions() != null ? med.getInstructions() : ""
        );

        String qrPayload = med.getQrPayload() != null
            ? med.getQrPayload()
            : "kindercare-connect:medication:" + med.getMedicationId();

        return new MedicationResponse(
            med.getId(),
            med.getMedicationId(),
            med.getChildId(),
            childName,
            med.getName(),
            med.getActiveIngredient() != null ? med.getActiveIngredient() : "",
            med.getDosage() != null ? med.getDosage() : "",
            med.getInstructions() != null ? med.getInstructions() : "",
            schedule,
            history,
            qrPayload,
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
