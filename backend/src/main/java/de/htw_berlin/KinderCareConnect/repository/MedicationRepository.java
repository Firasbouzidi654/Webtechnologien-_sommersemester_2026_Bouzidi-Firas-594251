package de.htw_berlin.KinderCareConnect.repository;

import de.htw_berlin.KinderCareConnect.entity.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<MedicationEntity, Long> {

    List<MedicationEntity> findByChildIdOrderById(Long childId);

    Optional<MedicationEntity> findByMedicationId(String medicationId);

    void deleteByMedicationId(String medicationId);

    List<MedicationEntity> findAllByOrderByChildIdAscScheduledTimeAsc();

    long countByMedicationIdStartingWith(String prefix);
}
