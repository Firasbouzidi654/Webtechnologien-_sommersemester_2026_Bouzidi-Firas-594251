package de.htw_berlin.KinderCareConnect.business.mapping;

import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.rest.model.ChildResponse;
import org.springframework.stereotype.Component;

@Component
public class ChildRestMapper {

    public ChildResponse toResponse(ChildEntity childEntity) {
        return new ChildResponse(
            childEntity.getId(),
            childEntity.getName(),
            childEntity.getDateOfBirth(),
            childEntity.getAllergies(),
            childEntity.getChronicDiseases(),
            childEntity.getSpecialNeeds(),
            childEntity.getEmergencyContact()
        );
    }
}
