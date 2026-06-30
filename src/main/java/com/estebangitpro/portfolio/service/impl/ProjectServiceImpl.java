package com.estebangitpro.portfolio.service.impl;

import com.estebangitpro.portfolio.dto.ProjectRequestDTO;
import com.estebangitpro.portfolio.dto.ProjectResponseDTO;
import com.estebangitpro.portfolio.mapper.ProjectMapper;
import com.estebangitpro.portfolio.model.Project;
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

       Project project = mapper.toProject(requestDTO);
       repository.save(project);
        return mapper.toProjectResponseDTO(project);
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        return repository.findByPublishedTrueOrderByOrderAsc()
                .stream()
                .map(mapper::toProjectResponseDTO)
                .toList();
    }


    @Override
    public ProjectResponseDTO getProductById(String id) {
        Project project = repository.findById(id).orElseThrow(
                () -> new RuntimeException("Producto no encontrado con el id : " +  id)
        );
        return mapper.toProjectResponseDTO(project);
    }

    @Override
    public ProjectResponseDTO updateProject(String id, ProjectRequestDTO requestDTO) {
        Project project = repository.findById(id).orElseThrow();
        ProjectResponseDTO projectResponseDTO = mapper.toProjectResponseDTO(project);

        return null;
    }

    @Override
    public void deleteProject(String id) {
        Project project = repository.findById(id).orElseThrow();
        repository.delete(project);
    }

}
