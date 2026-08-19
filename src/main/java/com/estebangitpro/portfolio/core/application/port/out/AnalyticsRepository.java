package com.estebangitpro.portfolio.core.application.port.out;

import com.estebangitpro.portfolio.core.domain.Analytics;
import com.estebangitpro.portfolio.core.domain.AnalyticsType;

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
