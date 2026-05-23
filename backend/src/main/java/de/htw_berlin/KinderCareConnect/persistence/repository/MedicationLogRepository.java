package de.htw_berlin.KinderCareConnect.persistence.repository;

import de.htw_berlin.KinderCareConnect.persistence.entity.MedicationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationLogRepository extends JpaRepository<MedicationLogEntity, Long> {

    List<MedicationLogEntity> findByMedicationIdOrderByLoggedAtDesc(String medicationId);

    List<MedicationLogEntity> findByChildIdOrderByLoggedAtDesc(Long childId);
}
