package com.estebangitpro.portfolio.dto;

import com.estebangitpro.portfolio.model.ProjectLinks;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.model.ProjectTranslation;
import com.estebangitpro.portfolio.model.StrategyItem;
import com.estebangitpro.portfolio.model.TechStackItem;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Create/update payload. {@code order} and {@code published} are boxed on purpose:
 * as primitives, omitting them made Jackson fail the whole request instead of falling
 * back to a default. The mapper supplies 0 and false when they are absent.
 *
 * <p>{@code translations} carries the copy in other languages, keyed by language code
 * ({@code "en"}). Omit it on an update to keep the translations already stored; send
 * {@code {}} to remove them.
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
        LocalDate endDate,
        Map<String, ProjectTranslation> translations
) {
}
