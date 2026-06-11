package de.htw_berlin.KinderCareConnect.service;

import de.htw_berlin.KinderCareConnect.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.entity.MedicationEntity;
import de.htw_berlin.KinderCareConnect.entity.MedicationLogEntity;
import de.htw_berlin.KinderCareConnect.exception.ResourceNotFoundException;
import de.htw_berlin.KinderCareConnect.model.DailyStatsResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationLogResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationScheduleResponse;
import de.htw_berlin.KinderCareConnect.model.MedicationTaskResponse;
import de.htw_berlin.KinderCareConnect.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.repository.MedicationLogRepository;
import de.htw_berlin.KinderCareConnect.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class MedicationService {

    private static final String PENDING = "Pending";
    private static final String TAKEN = "Taken";
    private static final String MISSED = "Missed";

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final ChildRepository childRepository;

    public MedicationService(
        MedicationRepository medicationRepository,
        MedicationLogRepository medicationLogRepository,
        ChildRepository childRepository
    ) {
        this.medicationRepository = medicationRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.childRepository = childRepository;
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findAllByOrderByChildIdAscScheduledTimeAsc()
            .stream()
            .map(this::toMedicationResponse)
            .toList();
    }

    public List<MedicationResponse> getMedicationsByChild(Long childId) {
        return medicationRepository.findByChildIdOrderById(childId)
            .stream()
            .map(this::toMedicationResponse)
            .toList();
    }

    @Transactional
    public MedicationResponse createMedication(Long childId, Map<String, Object> data) {
        ChildEntity child = childRepository.findById(childId)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found: " + childId));

        String medicationId = generateMedicationId();

        MedicationEntity med = new MedicationEntity();
        med.setMedicationId(medicationId);
        med.setChildId(childId);
        med.setName((String) data.getOrDefault("name", ""));
        med.setActiveIngredient((String) data.getOrDefault("activeIngredient", ""));
        med.setDosage((String) data.getOrDefault("dosage", ""));
        med.setInstructions((String) data.getOrDefault("instructions", ""));
        med.setFrequency((String) data.getOrDefault("frequency", "Daily"));
        med.setDayPart((String) data.getOrDefault("dayPart", "Morning"));
        med.setScheduledTime((String) data.getOrDefault("scheduledTime", "08:00"));
        med.setTodayStatus(PENDING);
        med.setPrescriptionUploaded(false);

        return toMedicationResponseWithChild(medicationRepository.save(med), child.getName());
    }

    @Transactional
    public MedicationResponse updateMedication(String medicationId, Map<String, Object> data) {
        MedicationEntity med = medicationRepository.findByMedicationId(medicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Medication not found: " + medicationId));

        if (data.containsKey("name")) med.setName((String) data.get("name"));
        if (data.containsKey("activeIngredient")) med.setActiveIngredient((String) data.get("activeIngredient"));
        if (data.containsKey("dosage")) med.setDosage((String) data.get("dosage"));
        if (data.containsKey("instructions")) med.setInstructions((String) data.get("instructions"));
        if (data.containsKey("frequency")) med.setFrequency((String) data.get("frequency"));
        if (data.containsKey("dayPart")) med.setDayPart((String) data.get("dayPart"));
        if (data.containsKey("scheduledTime")) med.setScheduledTime((String) data.get("scheduledTime"));
        if (data.containsKey("prescriptionUploaded")) {
            med.setPrescriptionUploaded(Boolean.TRUE.equals(data.get("prescriptionUploaded")));
        }

        return toMedicationResponse(medicationRepository.save(med));
    }

    @Transactional
    public MedicationTaskResponse markMedicationTaken(String medicationId) {
        MedicationEntity med = medicationRepository.findByMedicationId(medicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Medication not found: " + medicationId));

        med.setTodayStatus(TAKEN);
        medicationRepository.save(med);

        MedicationLogEntity log = new MedicationLogEntity();
        log.setMedicationId(medicationId);
        log.setChildId(med.getChildId());
        log.setStatus(TAKEN);
        log.setAdminName("Admin");
        log.setLoggedAt(LocalDateTime.now().toString());
        log.setNote("Confirmed via admin dashboard");
        medicationLogRepository.save(log);

        return toTaskResponse(med);
    }

    @Transactional
    public MedicationResponse updateMedicationStatus(String medicationId, String status) {
        MedicationEntity med = medicationRepository.findByMedicationId(medicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Medication not found: " + medicationId));

        med.setTodayStatus(status);
        medicationRepository.save(med);

        if (TAKEN.equals(status) || MISSED.equals(status)) {
            MedicationLogEntity log = new MedicationLogEntity();
            log.setMedicationId(medicationId);
            log.setChildId(med.getChildId());
            log.setStatus(status);
            log.setAdminName("Admin");
            log.setLoggedAt(LocalDateTime.now().toString());
            log.setNote("");
            medicationLogRepository.save(log);
        }

        return toMedicationResponse(med);
    }

    @Transactional
    public void deleteMedication(String medicationId) {
        MedicationEntity med = medicationRepository.findByMedicationId(medicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Medication not found: " + medicationId));
        medicationRepository.delete(med);
    }

    public List<MedicationTaskResponse> getTodayTasks() {
        return medicationRepository.findAllByOrderByChildIdAscScheduledTimeAsc()
            .stream()
            .map(this::toTaskResponseWithReminderCheck)
            .toList();
    }

    public DailyStatsResponse getTodayStats() {
        List<MedicationEntity> all = medicationRepository.findAll();
        long pending = all.stream().filter(m -> PENDING.equals(m.getTodayStatus())).count();
        long taken = all.stream().filter(m -> TAKEN.equals(m.getTodayStatus())).count();
        long missed = all.stream().filter(m -> MISSED.equals(m.getTodayStatus())).count();
        return new DailyStatsResponse(pending, taken, missed);
    }

    // ─── Mapping helpers ────────────────────────────────────────────────────────

    private MedicationResponse toMedicationResponse(MedicationEntity med) {
        Long childId = med.getChildId();
        String childName;
        if (childId != null) {
            childName = childRepository.findById(childId)
                .map(ChildEntity::getName)
                .orElse("Unknown");
        } else {
            childName = "Unknown";
        }
        return toMedicationResponseWithChild(med, childName);
    }

    private MedicationResponse toMedicationResponseWithChild(MedicationEntity med, String childName) {
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
            med.getTodayStatus() != null ? med.getTodayStatus() : PENDING
        );
    }

    private MedicationTaskResponse toTaskResponse(MedicationEntity med) {
        ChildEntity child = childRepository.findById(med.getChildId()).orElse(null);
        String childName = child != null ? child.getName() : "Unknown";
        String groupName = child != null && child.getGroupName() != null ? child.getGroupName() : "General";

        return new MedicationTaskResponse(
            "TASK-" + med.getMedicationId(),
            med.getMedicationId(),
            med.getChildId(),
            childName,
            groupName,
            med.getName() != null ? med.getName() : "",
            med.getDosage() != null ? med.getDosage() : "",
            med.getScheduledTime() != null ? med.getScheduledTime() : "12:00",
            med.getInstructions() != null ? med.getInstructions() : "",
            med.getTodayStatus() != null ? med.getTodayStatus() : PENDING,
            false
        );
    }

    private MedicationTaskResponse toTaskResponseWithReminderCheck(MedicationEntity med) {
        MedicationTaskResponse task = toTaskResponse(med);
        boolean reminderDue = PENDING.equals(task.status())
            && med.getScheduledTime() != null
            && LocalTime.now().isAfter(LocalTime.parse(med.getScheduledTime()));
        if (!reminderDue) return task;
        return new MedicationTaskResponse(
            task.taskId(), task.medicationId(), task.childId(), task.childName(),
            task.groupName(), task.medicationName(), task.dosage(), task.scheduledTime(),
            task.instructions(), task.status(), true
        );
    }

    private String generateMedicationId() {
        long count = medicationRepository.countByMedicationIdStartingWith("MED-");
        return "MED-" + String.format("%03d", count + 1);
    }
}
