package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.exception.ProjectNotFoundException;
import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.model.ProjectTranslation;
import com.estebangitpro.portfolio.dto.ProjectDraft;
import com.estebangitpro.portfolio.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests: no Spring context, no database. The repository is a fake and the clock
 * is fixed, so every assertion is about behaviour rather than infrastructure.
 */
class ProjectServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-03-20T18:30:00Z");

    private InMemoryProjectRepository repository;
    private MutableClock clock;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProjectRepository();
        clock = new MutableClock(CREATED_AT);
        service = new ProjectService(repository, clock);
    }

    @Test
    void stamps_both_timestamps_when_creating() {
        Project created = service.createProject(draft("Mi Portfolio Web"));

        LocalDateTime expected = LocalDateTime.ofInstant(CREATED_AT, ZoneOffset.UTC);
        assertThat(created.getCreatedAt()).isEqualTo(expected);
        assertThat(created.getUpdatedAt()).isEqualTo(expected);
    }

    @Test
    void moves_updatedAt_but_preserves_createdAt_when_updating() {
        Project created = service.createProject(draft("Mi Portfolio Web"));
        clock.moveTo(UPDATED_AT);

        Project updated = service.updateProject(created.getId(), draft("Mi Portfolio Web"));

        assertThat(updated.getCreatedAt())
                .isEqualTo(LocalDateTime.ofInstant(CREATED_AT, ZoneOffset.UTC));
        assertThat(updated.getUpdatedAt())
                .isEqualTo(LocalDateTime.ofInstant(UPDATED_AT, ZoneOffset.UTC));
    }

    @Test
    void derives_the_slug_from_the_title() {
        assertThat(service.createProject(draft("Mi Portfolio Web")).getSlug())
                .isEqualTo("mi-portfolio-web");
        assertThat(service.createProject(draft("API   REST  con  Spring!")).getSlug())
                .isEqualTo("api-rest-con-spring");
    }

    @Test
    void rejects_operations_on_a_project_that_does_not_exist() {
        assertThatThrownBy(() -> service.getProjectById("missing"))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("missing");

        assertThatThrownBy(() -> service.updateProject("missing", draft("X")))
                .isInstanceOf(ProjectNotFoundException.class);

        assertThatThrownBy(() -> service.deleteProject("missing"))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void does_not_delete_anything_when_the_project_is_missing() {
        service.createProject(draft("Mi Portfolio Web"));

        assertThatThrownBy(() -> service.deleteProject("missing"))
                .isInstanceOf(ProjectNotFoundException.class);

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void stores_translations_under_a_lowercase_language_code() {
        Project created = service.createProject(
                draft("Mi Portfolio Web", Map.of(" EN ", english("My Portfolio Site"))));

        assertThat(created.getTranslations()).containsOnlyKeys("en");
        assertThat(created.getTranslations().get("en").title()).isEqualTo("My Portfolio Site");
    }

    @Test
    void a_project_created_without_translations_has_none() {
        Project created = service.createProject(draft("Mi Portfolio Web"));

        assertThat(created.getTranslations()).isEmpty();
    }

    @Test
    void an_update_that_omits_translations_keeps_the_stored_ones() {
        Project created = service.createProject(
                draft("Mi Portfolio Web", Map.of("en", english("My Portfolio Site"))));

        Project updated = service.updateProject(created.getId(), draft("Mi Portfolio Web"));

        assertThat(updated.getTranslations().get("en").title()).isEqualTo("My Portfolio Site");
    }

    @Test
    void an_update_with_an_empty_map_removes_the_translations() {
        Project created = service.createProject(
                draft("Mi Portfolio Web", Map.of("en", english("My Portfolio Site"))));

        Project updated = service.updateProject(created.getId(), draft("Mi Portfolio Web", Map.of()));

        assertThat(updated.getTranslations()).isEmpty();
    }

    @Test
    void an_update_with_translations_replaces_the_stored_ones() {
        Project created = service.createProject(
                draft("Mi Portfolio Web", Map.of("en", english("My Portfolio Site"))));

        Project updated = service.updateProject(created.getId(),
                draft("Mi Portfolio Web", Map.of("en", english("My Portfolio"))));

        assertThat(updated.getTranslations().get("en").title()).isEqualTo("My Portfolio");
    }

    private ProjectDraft draft(String title) {
        return draft(title, null);
    }

    private ProjectDraft draft(String title, Map<String, ProjectTranslation> translations) {
        return new ProjectDraft(
                title, "Resumen", "Descripcion", ProjectStatus.COMPLETADO,
                List.of(), null, null, null, List.of(), List.of(), List.of(),
                null, 0, true, null, null, translations);
    }

    private ProjectTranslation english(String title) {
        return new ProjectTranslation(title, "Summary", "Description", null, null, null);
    }

    /** Fake out-port: the whole point of depending on the interface. */
    private static final class InMemoryProjectRepository implements ProjectRepository {
        private final List<Project> stored = new ArrayList<>();
        private int sequence;

        @Override
        public Project save(Project project) {
            Project persisted = project.getId() == null ? withId(project, "id-" + (++sequence)) : project;
            stored.removeIf(p -> p.getId().equals(persisted.getId()));
            stored.add(persisted);
            return persisted;
        }

        @Override
        public Optional<Project> findById(String id) {
            return stored.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public List<Project> findAll() {
            return List.copyOf(stored);
        }

        @Override
        public List<Project> findAllPublished() {
            return stored.stream().filter(Project::isPublished).toList();
        }

        @Override
        public void deleteById(String id) {
            stored.removeIf(p -> p.getId().equals(id));
        }

        private Project withId(Project p, String id) {
            return new Project(id, p.getSlug(), p.getTitle(), p.getSummary(), p.getDescription(),
                    p.getStatus(), p.getTags(), p.getThumbnail(), p.getCoverImage(), p.getVideoUrl(),
                    p.getTechStack(), p.getStrategies(), p.getLearnings(), p.getLinks(),
                    p.getOrder(), p.isPublished(), p.getStartDate(), p.getEndDate(),
                    p.getTranslations(), p.getCreatedAt(), p.getUpdatedAt());
        }
    }

    /** Clock the test can advance, so "later" is a deliberate step and not a sleep. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void moveTo(Instant next) {
            this.instant = next;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
