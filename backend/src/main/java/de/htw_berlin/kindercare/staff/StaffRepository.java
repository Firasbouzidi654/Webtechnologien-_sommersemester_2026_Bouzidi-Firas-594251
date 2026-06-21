package de.htw_berlin.kindercare.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findAllByOrderByIdAsc();
}
