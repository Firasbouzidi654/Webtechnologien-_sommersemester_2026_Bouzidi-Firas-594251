package de.htw_berlin.KinderCareConnect.model;

// Simplified: removed duplicate dosage/instructions fields that exist on MedicationResponse
public record MedicationScheduleResponse(
    String frequency,
    String dayPart,
    String specificTime
) {
}
