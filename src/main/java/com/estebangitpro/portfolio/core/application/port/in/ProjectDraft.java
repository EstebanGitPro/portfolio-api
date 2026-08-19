package com.estebangitpro.portfolio.core.application.port.in;

import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;

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
