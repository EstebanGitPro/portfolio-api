package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.model.Project;

import java.util.List;

public interface ProjectQueryUseCase {
    List<Project> getAllPublishedProjects();
    List<Project> getAllProjectsIncludingUnpublished();
    Project getProjectById(String id);
}
