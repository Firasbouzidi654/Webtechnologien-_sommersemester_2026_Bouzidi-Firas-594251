package de.htw_berlin.kindercare.child;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChildService {
    private final ChildRepository repository;

    public ChildService(ChildRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public List<Child> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @NonNull
    public Child create(@NonNull Child child) {
        return repository.save(new Child(child.getName().trim(), child.getAllergies()));
    }

    @NonNull
    public Child update(@NonNull Long id, @NonNull Child changes) {
        Child child = findById(id);
        if (changes.getName() != null && !changes.getName().isBlank()) child.setName(changes.getName().trim());
        if (changes.getAllergies() != null) child.setAllergies(changes.getAllergies());
        return repository.save(child);
    }

    @NonNull
    public Child findById(@NonNull Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."));
    }

    public void delete(@NonNull Long id) {
        repository.delete(findById(id));
    }
}
