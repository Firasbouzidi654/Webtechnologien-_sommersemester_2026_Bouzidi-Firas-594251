package de.htw_berlin.kindercare.child;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    @NonNull
    List<Child> findAllByOrderByIdAsc();

    @NonNull
    List<Child> findByParentIdOrderByIdAsc(@NonNull Long parentId);

    boolean existsByIdAndParentId(@NonNull Long id, @NonNull Long parentId);
}
