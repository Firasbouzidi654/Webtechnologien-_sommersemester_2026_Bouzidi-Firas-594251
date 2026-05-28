package de.htw_berlin.KinderCareConnect.rest.model;

import java.util.List;

public record ChildHealthResponse(
    Long id,
    String name,
    String groupName,
    String dateOfBirth,
    String parentName,
    String parentEmail,
    List<String> allergies,
    List<String> chronicDiseases,
    String healthNotes,
    List<MedicationResponse> medications,
    List<EmergencyContactResponse> emergencyContacts,
    String photoUrl
) {
}
