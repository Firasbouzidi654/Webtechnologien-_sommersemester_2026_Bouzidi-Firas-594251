package de.htw_berlin.kindercare.medication;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findAllByOrderByIdAsc();
    List<Medication> findByChildName(String childName);
}
