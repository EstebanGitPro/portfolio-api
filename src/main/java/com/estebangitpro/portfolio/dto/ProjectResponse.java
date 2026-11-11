package com.estebangitpro.portfolio.dto;

import com.estebangitpro.portfolio.model.ProjectLinks;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.model.ProjectTranslation;
import com.estebangitpro.portfolio.model.StrategyItem;
import com.estebangitpro.portfolio.model.TechStackItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProjectResponse(
        String id,
        String slug,
        String title,
        String summary,
        String description,
        ProjectStatus status,
        List<String> tags,
        String thumbnail,
        String coverImage,
        String videoUrl,
        List<TechStackItem> techStack,
        List<StrategyItem> strategies,
        List<String> learnings,
        ProjectLinks links,
        int order,
        boolean published,
        LocalDate startDate,
        LocalDate endDate,
        Map<String, ProjectTranslation> translations,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
