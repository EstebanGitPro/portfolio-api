package com.estebangitpro.portfolio.core.application.port.out;

import com.estebangitpro.portfolio.core.domain.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    List<Project> findAllPublished();
    List<Project> findAll();
    Optional<Project> findById(String id);
    Project save(Project project);
    void deleteById(String id);
}
