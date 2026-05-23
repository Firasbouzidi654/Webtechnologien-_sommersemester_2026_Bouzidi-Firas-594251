package de.htw_berlin.KinderCareConnect.persistence.repository;

import de.htw_berlin.KinderCareConnect.persistence.entity.ParentNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentNoteRepository extends JpaRepository<ParentNoteEntity, Long> {

    Optional<ParentNoteEntity> findByChildId(Long childId);
}
