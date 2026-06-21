package de.htw_berlin.kindercare.child;

import org.springframework.stereotype.Service;

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
}
