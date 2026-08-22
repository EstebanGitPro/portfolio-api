package com.estebangitpro.portfolio.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectMongoRepository extends MongoRepository<ProjectMongoDocument, String> {
    List<ProjectMongoDocument> findByPublishedTrueOrderByOrderAsc();
}
