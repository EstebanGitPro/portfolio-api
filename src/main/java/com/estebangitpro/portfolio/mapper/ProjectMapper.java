package com.estebangitpro.portfolio.mapper;

import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.dto.ProjectDraft;
import com.estebangitpro.portfolio.dto.ProjectRequest;
import com.estebangitpro.portfolio.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Translates between the HTTP payloads and the domain. Used by the web adapter only —
 * the core never sees a DTO.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "order", source = "order", defaultValue = "0")
    @Mapping(target = "published", source = "published", defaultValue = "false")
    ProjectDraft toDraft(ProjectRequest requestDTO);

    ProjectResponse toResponse(Project project);
}
