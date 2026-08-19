package com.estebangitpro.portfolio.core.application.port.in;

import com.estebangitpro.portfolio.core.domain.Project;

import java.util.List;

public interface ProjectQueryUseCase {
    List<Project> getAllPublishedProjects();
    List<Project> getAllProjectsIncludingUnpublished();
    Project getProjectById(String id);
}
