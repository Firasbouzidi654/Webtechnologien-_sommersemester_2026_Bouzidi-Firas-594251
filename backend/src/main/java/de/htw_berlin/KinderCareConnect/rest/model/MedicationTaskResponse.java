package de.htw_berlin.KinderCareConnect.rest.model;

public record MedicationTaskResponse(
    String taskId,
    String medicationId,
    Long childId,
    String childName,
    String groupName,
    String medicationName,
    String dosage,
    String scheduledTime,
    String instructions,
    String status,
    boolean reminderDue,
    String qrPayload
) {
}
