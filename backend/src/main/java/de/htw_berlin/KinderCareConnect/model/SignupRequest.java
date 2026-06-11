package de.htw_berlin.KinderCareConnect.model;

public record SignupRequest(
    String fullName,
    String email,
    String password,
    String role,
    String phone
) {
}
