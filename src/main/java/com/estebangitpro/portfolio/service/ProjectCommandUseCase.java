package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.dto.ProjectDraft;

public interface ProjectCommandUseCase {
    Project createProject(ProjectDraft draft);
    Project updateProject(String id, ProjectDraft draft);
    void deleteProject(String id);
}
