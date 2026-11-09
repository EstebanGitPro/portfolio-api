package com.estebangitpro.portfolio.dto;

import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.model.ProjectLinks;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.model.StrategyItem;
import com.estebangitpro.portfolio.model.TechStackItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Data required to create or update a {@link Project}, expressed in domain terms.
 *
 * <p>It carries only the fields a caller may supply: identity ({@code id}, {@code slug})
 * and audit timestamps are derived by the domain, never provided from the outside.
 */
public record ProjectDraft(
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
        LocalDate endDate
) {
}
