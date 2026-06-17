package com.estebangitpro.portfolio.service.impl;

import com.estebangitpro.portfolio.dto.ProjectRequestDTO;
import com.estebangitpro.portfolio.dto.ProjectResponseDTO;
import com.estebangitpro.portfolio.mapper.ProjectMapper;
import com.estebangitpro.portfolio.repository.ProjectRepository;
import com.estebangitpro.portfolio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO) {
        return null;
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        return repository.findByPublishedTrueOrderByOrderAsc()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}
