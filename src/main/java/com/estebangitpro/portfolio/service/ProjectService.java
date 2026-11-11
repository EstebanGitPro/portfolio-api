package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.model.ProjectTranslation;
import com.estebangitpro.portfolio.dto.ProjectDraft;
import com.estebangitpro.portfolio.repository.ProjectRepository;
import com.estebangitpro.portfolio.service.ProjectCommandUseCase;
import com.estebangitpro.portfolio.service.ProjectQueryUseCase;
import com.estebangitpro.portfolio.exception.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService implements ProjectQueryUseCase, ProjectCommandUseCase {

    private final ProjectRepository repository;
    private final Clock clock;

    @Override
    public Project createProject(ProjectDraft draft) {
        LocalDateTime now = LocalDateTime.now(clock);
        return repository.save(fromDraft(null, generateSlug(draft.title()), draft,
                normalize(draft.translations()), now, now));
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
                // An update that says nothing about translations keeps the stored ones:
                // editing a project's status must not silently erase its English copy.
                draft.translations() == null
                        ? existing.getTranslations()
                        : normalize(draft.translations()),
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
                              Map<String, ProjectTranslation> translations,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Project(
                id, slug,
                draft.title(), draft.summary(), draft.description(),
                draft.status(), draft.tags(), draft.thumbnail(),
                draft.coverImage(), draft.videoUrl(), draft.techStack(),
                draft.strategies(), draft.learnings(), draft.links(),
                draft.order(), draft.published(), draft.startDate(),
                draft.endDate(), translations, createdAt, updatedAt
        );
    }

    /**
     * Language codes are stored lowercase and trimmed so "EN", "en " and "en" are one
     * language; entries without a code or without content are dropped.
     */
    private Map<String, ProjectTranslation> normalize(Map<String, ProjectTranslation> translations) {
        if (translations == null) {
            return Map.of();
        }
        Map<String, ProjectTranslation> normalized = new LinkedHashMap<>();
        translations.forEach((language, translation) -> {
            if (language != null && !language.isBlank() && translation != null) {
                normalized.put(language.strip().toLowerCase(Locale.ROOT), translation);
            }
        });
        return normalized;
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .strip();
    }
}
