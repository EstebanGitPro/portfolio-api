package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.application.port.out.ProjectRepository;
import com.estebangitpro.portfolio.core.domain.DuplicateProjectSlugException;
import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.support.MongoIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProjectMongoAdapterTest extends MongoIntegrationTest {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void clearProjects() {
        mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(),
                ProjectMongoDocument.class);
    }

    @Test
    void rejects_a_second_project_taking_an_existing_slug() {
        repository.save(project("mi-portfolio-web"));

        assertThatThrownBy(() -> repository.save(project("mi-portfolio-web")))
                .isInstanceOf(DuplicateProjectSlugException.class)
                .hasMessageContaining("mi-portfolio-web");

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void accepts_projects_with_distinct_slugs() {
        repository.save(project("primer-proyecto"));
        repository.save(project("segundo-proyecto"));

        assertThat(repository.findAll())
                .extracting(Project::getSlug)
                .containsExactlyInAnyOrder("primer-proyecto", "segundo-proyecto");
    }

    private Project project(String slug) {
        return new Project(
                null, slug, "Titulo", "Resumen", "Descripcion",
                ProjectStatus.COMPLETADO, List.of(), null, null, null,
                List.of(), List.of(), List.of(), null,
                0, true, null, null, null, null);
    }
}
