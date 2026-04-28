package de.htw_berlin.KinderCareConnect.rest.model;

public record MedicationScheduleResponse(
    Long id,
    String frequency,
    String dayPart,
    String specificTime,
    String dosage,
    String instructions
) {
}
