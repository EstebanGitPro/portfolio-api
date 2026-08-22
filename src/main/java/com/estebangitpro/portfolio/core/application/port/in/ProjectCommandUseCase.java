package com.estebangitpro.portfolio.core.application.port.in;

import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.application.port.in.ProjectDraft;

public interface ProjectCommandUseCase {
    Project createProject(ProjectDraft draft);
    Project updateProject(String id, ProjectDraft draft);
    void deleteProject(String id);
}
