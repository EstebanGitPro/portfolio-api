package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.dto.ProjectRequestDTO;
import com.estebangitpro.portfolio.dto.ProjectResponseDTO;

import java.util.List;

public interface ProjectService {
    ProjectResponseDTO createProject(ProjectRequestDTO requestDTO);
    List<ProjectResponseDTO> getAllProjects();
    ProjectResponseDTO getProductById(String Id);
    ProjectResponseDTO updateProject(String id, ProjectRequestDTO requestDTO);
    void deleteProject(String id);
}
