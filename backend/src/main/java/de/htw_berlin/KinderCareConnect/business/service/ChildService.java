package de.htw_berlin.KinderCareConnect.business.service;

import de.htw_berlin.KinderCareConnect.business.mapping.ChildRestMapper;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.rest.model.ChildResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final ChildRestMapper childRestMapper;

    public ChildService(ChildRepository childRepository, ChildRestMapper childRestMapper) {
        this.childRepository = childRepository;
        this.childRestMapper = childRestMapper;
    }

    public List<ChildResponse> getAllChildren() {
        return childRepository.findAllByOrderByIdAsc()
            .stream()
            .map(childRestMapper::toResponse)
            .toList();
    }
}
