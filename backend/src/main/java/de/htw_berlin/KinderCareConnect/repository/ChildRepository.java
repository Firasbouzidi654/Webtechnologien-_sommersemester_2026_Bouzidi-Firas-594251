package de.htw_berlin.KinderCareConnect.repository;

import de.htw_berlin.KinderCareConnect.entity.ChildEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<ChildEntity, Long> {

    List<ChildEntity> findAllByOrderByIdAsc();
}
