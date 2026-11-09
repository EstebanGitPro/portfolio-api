package com.estebangitpro.portfolio.repository;

import com.estebangitpro.portfolio.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    List<Project> findAllPublished();
    List<Project> findAll();
    Optional<Project> findById(String id);
    Project save(Project project);
    void deleteById(String id);
}
