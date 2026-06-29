package de.htw_berlin.kindercare.child;

import de.htw_berlin.kindercare.config.RoleAccess;
import de.htw_berlin.kindercare.user.User;
import de.htw_berlin.kindercare.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChildService {
    private final ChildRepository repository;
    private final UserRepository users;

    public ChildService(ChildRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @NonNull
    public List<Child> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @NonNull
    public List<Child> findVisibleTo(@Nullable String role, @Nullable String userId) {
        if (role == null || role.isBlank()) {
            return findAll();
        }

        RoleAccess.require(role, "PARENT", "STAFF", "ADMIN");
        if (RoleAccess.hasAny(role, "STAFF", "ADMIN")) {
            return findAll();
        }

        Long parentId = requireParentAccount(userId);
        return repository.findByParentIdOrderByIdAsc(parentId);
    }

    @NonNull
    public Child create(@NonNull Child child, @Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "ADMIN");
        Long parentId = RoleAccess.hasAny(role, "ADMIN") ? child.getParentId() : requireParentAccount(userId);
        if (parentId != null) {
            requireParentAccount(parentId);
        }

        return repository.save(new Child(child.getName().trim(), child.getAllergies(), parentId));
    }

    @NonNull
    public Child update(@NonNull Long id, @NonNull Child changes, @Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "ADMIN");
        Child child = findAccessibleById(id, role, userId);
        if (changes.getName() != null && !changes.getName().isBlank()) child.setName(changes.getName().trim());
        if (changes.getAllergies() != null) child.setAllergies(changes.getAllergies());
        if (RoleAccess.hasAny(role, "ADMIN") && changes.getParentId() != null) {
            requireParentAccount(changes.getParentId());
            child.setParentId(changes.getParentId());
        }
        return repository.save(child);
    }

    @NonNull
    public Child findById(@NonNull Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."));
    }

    @NonNull
    public Child findAccessibleById(@NonNull Long id, @Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "STAFF", "ADMIN");
        if (RoleAccess.hasAny(role, "STAFF", "ADMIN")) {
            return findById(id);
        }

        Long parentId = requireParentAccount(userId);
        if (!repository.existsByIdAndParentId(id, parentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found.");
        }
        return findById(id);
    }

    public void delete(@NonNull Long id, @Nullable String role, @Nullable String userId) {
        RoleAccess.require(role, "PARENT", "ADMIN");
        repository.delete(findAccessibleById(id, role, userId));
    }

    private Long requireParentAccount(@Nullable String userId) {
        return requireParentAccount(RoleAccess.requireUserId(userId));
    }

    private Long requireParentAccount(@NonNull Long parentId) {
        User parent = users.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "A valid parent account is required."));
        if (!"PARENT".equals(RoleAccess.normalize(parent.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Children can only be assigned to parent accounts.");
        }
        return parent.getId();
    }
}
