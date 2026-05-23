package de.htw_berlin.KinderCareConnect.business.error;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists.");
    }
}
