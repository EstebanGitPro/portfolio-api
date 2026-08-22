package com.estebangitpro.portfolio.core.application.service;

import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.application.port.in.ProjectDraft;
import com.estebangitpro.portfolio.core.application.port.out.ProjectRepository;
import com.estebangitpro.portfolio.core.application.port.in.ProjectCommandUseCase;
import com.estebangitpro.portfolio.core.application.port.in.ProjectQueryUseCase;
import com.estebangitpro.portfolio.core.application.exception.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService implements ProjectQueryUseCase, ProjectCommandUseCase {

    private final ProjectRepository repository;
    private final Clock clock;

    @Override
    public Project createProject(ProjectDraft draft) {
        LocalDateTime now = LocalDateTime.now(clock);
        return repository.save(fromDraft(null, generateSlug(draft.title()), draft, now, now));
    }

    @Override
    public List<Project> getAllPublishedProjects() {
        return repository.findAllPublished();
    }

    @Override
    public List<Project> getAllProjectsIncludingUnpublished() {
        return repository.findAll();
    }

    @Override
    public Project getProjectById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Override
    public Project updateProject(String id, ProjectDraft draft) {
        Project existing = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        return repository.save(fromDraft(
                existing.getId(),
                generateSlug(draft.title()),
                draft,
                existing.getCreatedAt(),
                LocalDateTime.now(clock)));
    }

    @Override
    public void deleteProject(String id) {
        repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        repository.deleteById(id);
    }

    private Project fromDraft(String id, String slug, ProjectDraft draft,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Project(
                id, slug,
                draft.title(), draft.summary(), draft.description(),
                draft.status(), draft.tags(), draft.thumbnail(),
                draft.coverImage(), draft.videoUrl(), draft.techStack(),
                draft.strategies(), draft.learnings(), draft.links(),
                draft.order(), draft.published(), draft.startDate(),
                draft.endDate(), createdAt, updatedAt
        );
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .strip();
    }
}
