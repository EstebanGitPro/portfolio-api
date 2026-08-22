package com.estebangitpro.portfolio.adapter.in.web.dto;

import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * Create/update payload. {@code order} and {@code published} are boxed on purpose:
 * as primitives, omitting them made Jackson fail the whole request instead of falling
 * back to a default. The mapper supplies 0 and false when they are absent.
 */
public record ProjectRequest(
        @NotBlank String title,
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
        Integer order,
        Boolean published,
        LocalDate startDate,
        LocalDate endDate
) {
}
