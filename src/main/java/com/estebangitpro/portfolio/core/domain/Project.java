package com.estebangitpro.portfolio.core.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Project {
    private final String id;
    private final String slug;
    private final String title;
    private final String summary;
    private final String description;
    private final ProjectStatus status;
    private final List<String> tags;
    private final String thumbnail;
    private final String coverImage;
    private final String videoUrl;
    private final List<TechStackItem> techStack;
    private final List<StrategyItem> strategies;
    private final List<String> learnings;
    private final ProjectLinks links;
    private final int order;
    private final boolean published;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Project(String id, String slug, String title, String summary, String description,
                   ProjectStatus status, List<String> tags, String thumbnail, String coverImage,
                   String videoUrl, List<TechStackItem> techStack, List<StrategyItem> strategies,
                   List<String> learnings, ProjectLinks links, int order, boolean published,
                   LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.status = status;
        this.tags = tags;
        this.thumbnail = thumbnail;
        this.coverImage = coverImage;
        this.videoUrl = videoUrl;
        this.techStack = techStack;
        this.strategies = strategies;
        this.learnings = learnings;
        this.links = links;
        this.order = order;
        this.published = published;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public ProjectStatus getStatus() { return status; }
    public List<String> getTags() { return tags; }
    public String getThumbnail() { return thumbnail; }
    public String getCoverImage() { return coverImage; }
    public String getVideoUrl() { return videoUrl; }
    public List<TechStackItem> getTechStack() { return techStack; }
    public List<StrategyItem> getStrategies() { return strategies; }
    public List<String> getLearnings() { return learnings; }
    public ProjectLinks getLinks() { return links; }
    public int getOrder() { return order; }
    public boolean isPublished() { return published; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
