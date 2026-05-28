package de.htw_berlin.KinderCareConnect.persistence.repository;

import de.htw_berlin.KinderCareConnect.business.error.ResourceNotFoundException;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import de.htw_berlin.KinderCareConnect.rest.model.DailyStatsResponse;
import de.htw_berlin.KinderCareConnect.rest.model.EmergencyContactResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationLogResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationScheduleResponse;
import de.htw_berlin.KinderCareConnect.rest.model.MedicationTaskResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HealthPrototypeRepository {

    private static final String TAKEN = "Taken";
    private static final String PENDING = "Pending";
    private static final String MISSED = "Missed";

    private final Map<Long, ChildCore> children = new LinkedHashMap<>();
    private final Map<Long, List<EmergencyContactResponse>> emergencyContactsByChildId = new LinkedHashMap<>();
    private final Map<Long, List<MedicationResponse>> medicationsByChildId = new LinkedHashMap<>();
    private final Map<String, MedicationTaskResponse> todayTasksByMedicationId = new LinkedHashMap<>();
    private long nextLogId = 900L;

    public HealthPrototypeRepository() {
        seedChildren();
        seedMedicationTasks();
    }

    public List<ChildHealthResponse> findAllChildren() {
        return children.keySet()
            .stream()
            .map(this::findChildById)
            .toList();
    }

    public ChildHealthResponse findChildById(Long id) {
        ChildCore child = children.get(id);
        if (child == null) {
            throw new ResourceNotFoundException("Child with id " + id + " was not found");
        }

        return new ChildHealthResponse(
            child.id(),
            child.name(),
            child.groupName(),
            child.dateOfBirth(),
            child.parentName(),
            child.parentEmail(),
            List.copyOf(child.allergies()),
            List.copyOf(child.chronicDiseases()),
            child.healthNotes(),
            List.copyOf(medicationsByChildId.getOrDefault(id, List.of())),
            List.copyOf(emergencyContactsByChildId.getOrDefault(id, List.of())),
            null
        );
    }

    public List<MedicationResponse> findAllMedications() {
        return medicationsByChildId.values()
            .stream()
            .flatMap(List::stream)
            .sorted(Comparator.comparing(MedicationResponse::medicationId))
            .toList();
    }

    public List<MedicationTaskResponse> findTodayMedicationTasks() {
        return todayTasksByMedicationId.values()
            .stream()
            .sorted(Comparator.comparing(MedicationTaskResponse::scheduledTime))
            .map(this::refreshReminderFlag)
            .toList();
    }

    public MedicationTaskResponse markMedicationTaken(String medicationId) {
        MedicationTaskResponse existingTask = todayTasksByMedicationId.get(medicationId);
        if (existingTask == null) {
            throw new ResourceNotFoundException("Medication with id " + medicationId + " was not found in today's tasks");
        }

        MedicationTaskResponse updatedTask = new MedicationTaskResponse(
            existingTask.taskId(),
            existingTask.medicationId(),
            existingTask.childId(),
            existingTask.childName(),
            existingTask.groupName(),
            existingTask.medicationName(),
            existingTask.dosage(),
            existingTask.scheduledTime(),
            existingTask.instructions(),
            TAKEN,
            false,
            existingTask.qrPayload()
        );

        todayTasksByMedicationId.put(medicationId, updatedTask);
        appendMedicationLog(updatedTask);
        return updatedTask;
    }

    public DailyStatsResponse getTodayStats() {
        List<MedicationTaskResponse> tasks = findTodayMedicationTasks();
        return new DailyStatsResponse(
            countByStatus(tasks, PENDING),
            countByStatus(tasks, TAKEN),
            countByStatus(tasks, MISSED)
        );
    }

    private long countByStatus(List<MedicationTaskResponse> tasks, String status) {
        return tasks.stream()
            .filter(task -> status.equals(task.status()))
            .count();
    }

    private MedicationTaskResponse refreshReminderFlag(MedicationTaskResponse task) {
        boolean reminderDue = PENDING.equals(task.status()) && LocalTime.now().isAfter(LocalTime.parse(task.scheduledTime()));
        return new MedicationTaskResponse(
            task.taskId(),
            task.medicationId(),
            task.childId(),
            task.childName(),
            task.groupName(),
            task.medicationName(),
            task.dosage(),
            task.scheduledTime(),
            task.instructions(),
            task.status(),
            reminderDue,
            task.qrPayload()
        );
    }

    private void appendMedicationLog(MedicationTaskResponse task) {
        List<MedicationResponse> medications = new ArrayList<>(medicationsByChildId.getOrDefault(task.childId(), List.of()));
        List<MedicationResponse> updatedMedications = new ArrayList<>();

        for (MedicationResponse medication : medications) {
            if (!medication.medicationId().equals(task.medicationId())) {
                updatedMedications.add(medication);
                continue;
            }

            List<MedicationLogResponse> updatedHistory = new ArrayList<>(medication.history());
            updatedHistory.add(0, new MedicationLogResponse(
                nextLogId++,
                task.medicationId(),
                task.childId(),
                TAKEN,
                "Mock Admin",
                LocalDateTime.now().toString(),
                "Confirmed from admin dashboard prototype"
            ));

            updatedMedications.add(new MedicationResponse(
                medication.id(),
                medication.medicationId(),
                medication.childId(),
                medication.childName(),
                medication.name(),
                medication.activeIngredient(),
                medication.dosage(),
                medication.instructions(),
                medication.schedule(),
                List.copyOf(updatedHistory),
                medication.qrPayload(),
                medication.prescriptionUploaded(),
                medication.todayStatus()
            ));
        }

        medicationsByChildId.put(task.childId(), updatedMedications);
    }

    private void seedChildren() {
        children.put(1L, new ChildCore(
            1L,
            "Adam Schneider",
            "Sunflowers",
            "2021-03-15",
            "Sara Schneider",
            "sara.schneider@example.com",
            List.of("Peanuts"),
            List.of("Mild asthma"),
            "Needs inhaler available during outdoor play."
        ));
        children.put(2L, new ChildCore(
            2L,
            "Noah Becker",
            "Sunflowers",
            "2020-07-22",
            "Jonas Becker",
            "jonas.becker@example.com",
            List.of("None known"),
            List.of("Asthma"),
            "Observe breathing after sports activities."
        ));
        children.put(3L, new ChildCore(
            3L,
            "Lina Wagner",
            "Rainbows",
            "2021-01-10",
            "Amira Wagner",
            "amira.wagner@example.com",
            List.of("Bee stings"),
            List.of("None"),
            "Emergency spray is stored in the office cabinet."
        ));

        emergencyContactsByChildId.put(1L, List.of(
            new EmergencyContactResponse(1L, "Sara Schneider", "Mother", "+49 151 123456", "sara.schneider@example.com", 1),
            new EmergencyContactResponse(2L, "Paul Schneider", "Father", "+49 152 222333", "paul.schneider@example.com", 2)
        ));
        emergencyContactsByChildId.put(2L, List.of(
            new EmergencyContactResponse(3L, "Jonas Becker", "Father", "+49 152 987654", "jonas.becker@example.com", 1),
            new EmergencyContactResponse(4L, "Mina Becker", "Mother", "+49 157 444555", "mina.becker@example.com", 2)
        ));
        emergencyContactsByChildId.put(3L, List.of(
            new EmergencyContactResponse(5L, "Amira Wagner", "Mother", "+49 176 456789", "amira.wagner@example.com", 1)
        ));

        medicationsByChildId.put(1L, List.of(
            medication(101L, "MED-001", 1L, "Adam Schneider", "Asthma Spray", "Salbutamol", "1 puff", "Shake before use.", "08:30", "Morning", true)
        ));
        medicationsByChildId.put(2L, List.of(
            medication(102L, "MED-002", 2L, "Noah Becker", "Asthma Spray", "Salbutamol", "1 puff", "Use spacer and monitor breathing.", "10:00", "Specific time", false),
            medication(103L, "MED-003", 2L, "Noah Becker", "Vitamin D Drops", "Colecalciferol", "2 drops", "Give after snack.", "12:30", "Daily", true)
        ));
        medicationsByChildId.put(3L, List.of(
            medication(104L, "MED-004", 3L, "Lina Wagner", "Allergy Spray", "Antihistamine", "1 spray", "Use only if symptoms appear.", "09:15", "Morning", true)
        ));
    }

    private MedicationResponse medication(
        Long id,
        String medicationId,
        Long childId,
        String childName,
        String name,
        String activeIngredient,
        String dosage,
        String instructions,
        String specificTime,
        String dayPart,
        boolean prescriptionUploaded
    ) {
        MedicationScheduleResponse schedule = new MedicationScheduleResponse(
            id + 1000,
            "Daily",
            dayPart,
            specificTime,
            dosage,
            instructions
        );

        return new MedicationResponse(
            id,
            medicationId,
            childId,
            childName,
            name,
            activeIngredient,
            dosage,
            instructions,
            schedule,
            List.of(new MedicationLogResponse(
                id + 2000,
                medicationId,
                childId,
                TAKEN,
                "M. Keller",
                "2026-04-27T" + specificTime + ":00",
                "Sample previous-day log"
            )),
            "kindercare-connect:medication:" + medicationId,
            prescriptionUploaded,
            PENDING
        );
    }

    private void seedMedicationTasks() {
        todayTasksByMedicationId.put("MED-001", task("TASK-001", "MED-001", 1L, "Adam Schneider", "Sunflowers", "Asthma Spray", "1 puff", "08:30", "Shake before use.", PENDING));
        todayTasksByMedicationId.put("MED-002", task("TASK-002", "MED-002", 2L, "Noah Becker", "Sunflowers", "Asthma Spray", "1 puff", "10:00", "Use spacer and monitor breathing.", TAKEN));
        todayTasksByMedicationId.put("MED-003", task("TASK-003", "MED-003", 2L, "Noah Becker", "Sunflowers", "Vitamin D Drops", "2 drops", "12:30", "Give after snack.", PENDING));
        todayTasksByMedicationId.put("MED-004", task("TASK-004", "MED-004", 3L, "Lina Wagner", "Rainbows", "Allergy Spray", "1 spray", "09:15", "Use only if symptoms appear.", MISSED));
    }

    private MedicationTaskResponse task(
        String taskId,
        String medicationId,
        Long childId,
        String childName,
        String groupName,
        String medicationName,
        String dosage,
        String scheduledTime,
        String instructions,
        String status
    ) {
        return new MedicationTaskResponse(
            taskId,
            medicationId,
            childId,
            childName,
            groupName,
            medicationName,
            dosage,
            scheduledTime,
            instructions,
            status,
            false,
            "kindercare-connect:medication:" + medicationId
        );
    }

    private record ChildCore(
        Long id,
        String name,
        String groupName,
        String dateOfBirth,
        String parentName,
        String parentEmail,
        List<String> allergies,
        List<String> chronicDiseases,
        String healthNotes
    ) {
    }
}
