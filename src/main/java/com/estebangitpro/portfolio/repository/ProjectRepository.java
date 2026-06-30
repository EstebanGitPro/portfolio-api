package com.estebangitpro.portfolio.repository;

import com.estebangitpro.portfolio.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByPublishedTrueOrderByOrderAsc();

}
