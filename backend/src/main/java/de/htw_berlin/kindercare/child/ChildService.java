package de.htw_berlin.kindercare.child;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChildService {
    private final ChildRepository repository;

    public ChildService(ChildRepository repository) {
        this.repository = repository;
    }

    public List<Child> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public Child create(Child child) {
        return repository.save(new Child(child.getName().trim(), child.getAllergies()));
    }

    public Child update(Long id, Child changes) {
        Child child = findById(id);
        if (changes.getName() != null && !changes.getName().isBlank()) child.setName(changes.getName().trim());
        if (changes.getAllergies() != null) child.setAllergies(changes.getAllergies());
        return repository.save(child);
    }

    public Child findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Child not found."));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
