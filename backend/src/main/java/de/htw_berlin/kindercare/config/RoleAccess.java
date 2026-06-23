package de.htw_berlin.kindercare.config;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

public final class RoleAccess {
    private RoleAccess() { }

    public static void require(@Nullable String role, String... allowedRoles) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(normalizedRole::equals);

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this action.");
        }
    }
}
