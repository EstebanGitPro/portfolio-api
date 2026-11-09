package com.estebangitpro.portfolio.repository.impl;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectMongoRepository extends MongoRepository<ProjectMongoDocument, String> {
    List<ProjectMongoDocument> findByPublishedTrueOrderByOrderAsc();
}
