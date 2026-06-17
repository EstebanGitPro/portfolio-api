package com.estebangitpro.portfolio.dto;

import com.estebangitpro.portfolio.model.ProjectLinks;
import com.estebangitpro.portfolio.model.StrategyItem;
import com.estebangitpro.portfolio.model.TechStackItem;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

public record ProjectRequestDTO(
        @NotBlank String title,
        String slug,
        String summary,
        String description,
        String status,
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
        Date startDate,
        Date endDate
) {
}
