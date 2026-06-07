package de.htw_berlin.KinderCareConnect.service;

import de.htw_berlin.KinderCareConnect.business.error.ResourceNotFoundException;
import de.htw_berlin.KinderCareConnect.business.service.ChildService;
import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.EmergencyContactRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.MedicationLogRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.MedicationRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.ParentNoteRepository;
import de.htw_berlin.KinderCareConnect.rest.model.ChildHealthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildServiceTest {

    // Mocked repository dependencies – no real database is used
    @Mock
    private ChildRepository childRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private MedicationLogRepository medicationLogRepository;

    @Mock
    private ParentNoteRepository parentNoteRepository;

    // The real service with the mocks injected above
    @InjectMocks
    private ChildService childService;

    @Test
    void getAllChildren_returnsChildrenFromRepository() {
        // given – repository returns one child
        ChildEntity child = new ChildEntity(
            "Emma Müller", LocalDate.of(2020, 3, 15),
            "Peanuts", null, null, null
        );
        when(childRepository.findAllByOrderByIdAsc()).thenReturn(List.of(child));
        when(medicationRepository.findByChildIdOrderById(any())).thenReturn(List.of());
        when(emergencyContactRepository.findByChildIdOrderByPriority(any())).thenReturn(List.of());

        // when
        List<ChildHealthResponse> result = childService.getAllChildren();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Emma Müller");
    }

    @Test
    void createChild_throwsException_whenNameIsBlank() {
        // given – child with an empty name
        ChildEntity child = new ChildEntity();
        child.setName("   ");
        child.setDateOfBirth(LocalDate.of(2020, 1, 1));

        // when / then – service must reject it
        assertThatThrownBy(() -> childService.createChild(child))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name is required");
    }

    @Test
    void getChildById_throwsException_whenChildDoesNotExist() {
        // given – repository has no child with id 99
        when(childRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then – service must throw ResourceNotFoundException
        assertThatThrownBy(() -> childService.getChildById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
