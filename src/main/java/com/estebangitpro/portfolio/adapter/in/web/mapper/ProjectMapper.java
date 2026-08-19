package com.estebangitpro.portfolio.adapter.in.web.mapper;

import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.application.port.in.ProjectDraft;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectRequest;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectResponse;
import org.mapstruct.Mapper;

/**
 * Translates between the HTTP payloads and the domain. Used by the web adapter only —
 * the core never sees a DTO.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectDraft toDraft(ProjectRequest requestDTO);

    ProjectResponse toResponse(Project project);
}
