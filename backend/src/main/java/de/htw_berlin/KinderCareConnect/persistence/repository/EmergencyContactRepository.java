package de.htw_berlin.KinderCareConnect.persistence.repository;

import de.htw_berlin.KinderCareConnect.persistence.entity.EmergencyContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, Long> {

    List<EmergencyContactEntity> findByChildIdOrderByPriority(Long childId);

    void deleteAllByChildId(Long childId);
}
