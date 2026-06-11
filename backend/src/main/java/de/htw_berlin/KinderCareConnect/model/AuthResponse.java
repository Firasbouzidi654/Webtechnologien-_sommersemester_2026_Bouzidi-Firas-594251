package de.htw_berlin.KinderCareConnect.model;

public record AuthResponse(
    Long id,
    String fullName,
    String email,
    String role,
    String phone,
    String createdAt
) {
}
