package de.htw_berlin.KinderCareConnect.rest.model;

public record EmergencyContactResponse(
    Long id,
    String name,
    String relationship,
    String phone,
    String email,
    int priority
) {
}
