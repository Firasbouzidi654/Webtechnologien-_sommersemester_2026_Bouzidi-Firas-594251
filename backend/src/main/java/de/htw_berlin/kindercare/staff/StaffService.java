package de.htw_berlin.kindercare.staff;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffRepository repository;

    public StaffService(StaffRepository repository) {
        this.repository = repository;
    }

    public List<Staff> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    public Staff create(Staff staff) {
        return repository.save(new Staff(staff.getName().trim(), staff.getRole()));
    }
}
