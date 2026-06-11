package de.htw_berlin.KinderCareConnect.model;

import java.util.List;

public record MedicationResponse(
    Long id,
    String medicationId,
    Long childId,
    String childName,
    String name,
    String activeIngredient,
    String dosage,
    String instructions,
    MedicationScheduleResponse schedule,
    List<MedicationLogResponse> history,
    boolean prescriptionUploaded,
    String todayStatus
) {
}
