package com.estebangitpro.portfolio.mapper;

import com.estebangitpro.portfolio.dto.ProjectRequestDTO;
import com.estebangitpro.portfolio.dto.ProjectResponseDTO;
import com.estebangitpro.portfolio.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    Project toProject(ProjectRequestDTO requestDTO);
    ProjectResponseDTO toProjectResponseDTO(Project project);
}
