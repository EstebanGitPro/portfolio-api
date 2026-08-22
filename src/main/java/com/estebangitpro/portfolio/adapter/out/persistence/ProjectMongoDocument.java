package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(value = "projects")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProjectMongoDocument {
    @Id
    private String id;
    private String slug;
    private String title;
    private String summary;
    private String description;
    private ProjectStatus status;
    private List<String> tags;
    private String thumbnail;
    private String coverImage;
    private String videoUrl;
    private List<TechStackItem> techStack;
    private List<StrategyItem> strategies;
    private List<String> learnings;
    private ProjectLinks links;
    @Builder.Default
    private int order = 0;
    @Builder.Default
    private boolean published = true;
    private LocalDate startDate;
    private LocalDate endDate;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
