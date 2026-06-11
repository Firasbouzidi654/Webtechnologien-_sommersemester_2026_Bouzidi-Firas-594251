package de.htw_berlin.KinderCareConnect.model;

public record LoginRequest(
    String email,
    String password
) {
}
