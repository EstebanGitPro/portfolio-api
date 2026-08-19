package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.domain.AnalyticsType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsMongoRepository extends MongoRepository<AnalyticsMongoDocument, String> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByType(AnalyticsType type);
    long countByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end);
    List<AnalyticsMongoDocument> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<AnalyticsMongoDocument> findByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end);
}
