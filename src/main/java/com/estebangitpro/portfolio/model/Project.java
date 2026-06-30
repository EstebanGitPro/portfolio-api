package com.estebangitpro.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class Project {
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
