package de.htw_berlin.KinderCareConnect.rest.model.auth;

public record SignupRequest(
    String fullName,
    String email,
    String password,
    String role,
    String phone
) {
}
