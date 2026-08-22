package com.estebangitpro.portfolio.core.application.service;

import com.estebangitpro.portfolio.core.application.exception.ProjectNotFoundException;
import com.estebangitpro.portfolio.core.application.port.in.ProjectDraft;
import com.estebangitpro.portfolio.core.application.port.out.ProjectRepository;
import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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

    private ProjectDraft draft(String title) {
        return new ProjectDraft(
                title, "Resumen", "Descripcion", ProjectStatus.COMPLETADO,
                List.of(), null, null, null, List.of(), List.of(), List.of(),
                null, 0, true, null, null);
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
                    p.getCreatedAt(), p.getUpdatedAt());
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
