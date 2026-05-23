package de.htw_berlin.KinderCareConnect.rest.model.auth;

public record LoginRequest(
    String email,
    String password
) {
}
