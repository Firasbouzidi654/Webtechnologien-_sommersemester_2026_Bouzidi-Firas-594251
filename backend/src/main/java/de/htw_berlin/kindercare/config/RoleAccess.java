package de.htw_berlin.kindercare.config;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

public final class RoleAccess {
    private RoleAccess() { }

    public static void require(@Nullable String role, String... allowedRoles) {
        String normalizedRole = normalize(role);
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(normalizedRole::equals);

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this action.");
        }
    }

    public static String normalize(@Nullable String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }

    public static boolean hasAny(@Nullable String role, String... allowedRoles) {
        String normalizedRole = normalize(role);
        return Arrays.stream(allowedRoles).anyMatch(normalizedRole::equals);
    }

    public static Long requireUserId(@Nullable String userId) {
        try {
            Long parsedUserId = userId == null ? null : Long.valueOf(userId.trim());
            if (parsedUserId != null && parsedUserId > 0) {
                return parsedUserId;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to the shared forbidden response below.
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A valid user account is required for this action.");
    }
}
