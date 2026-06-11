package de.htw_berlin.KinderCareConnect.model;

public record EmergencyContactResponse(
    Long id,
    String name,
    String relationship,
    String phone,
    String email,
    int priority
) {
}
