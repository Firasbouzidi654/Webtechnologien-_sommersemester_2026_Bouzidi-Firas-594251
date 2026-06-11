package de.htw_berlin.KinderCareConnect.model;

public record MedicationLogResponse(
    Long id,
    String medicationId,
    Long childId,
    String status,
    String adminName,
    String loggedAt,
    String note
) {
}
