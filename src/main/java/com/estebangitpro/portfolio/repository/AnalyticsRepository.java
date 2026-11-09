package com.estebangitpro.portfolio.repository;

import com.estebangitpro.portfolio.model.Analytics;
import com.estebangitpro.portfolio.model.AnalyticsType;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository {
    Analytics save(Analytics analytics);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByType(AnalyticsType type);
    long countByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end);
    List<Analytics> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Analytics> findByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end);
}
