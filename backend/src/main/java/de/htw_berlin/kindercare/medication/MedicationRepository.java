package de.htw_berlin.kindercare.medication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    @NonNull
    List<Medication> findAllByOrderByIdAsc();

    @NonNull
    List<Medication> findByChildId(@NonNull Long childId);
}
