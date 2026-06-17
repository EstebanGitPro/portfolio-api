package com.estebangitpro.portfolio.repository;

import com.estebangitpro.portfolio.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
}
