package com.estebangitpro.portfolio.repository;

import com.estebangitpro.portfolio.model.DuplicateProjectSlugException;
import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.model.ProjectTranslation;
import com.estebangitpro.portfolio.repository.impl.ProjectMongoDocument;
import com.estebangitpro.portfolio.support.MongoIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;

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

    @Test
    void round_trips_translations() {
        Project saved = repository.save(new Project(
                null, "mini-banco", "Mini Banco", "Resumen", "Descripcion",
                ProjectStatus.COMPLETADO, List.of(), null, null, null,
                List.of(), List.of(), List.of(), null,
                0, true, null, null,
                Map.of("en", new ProjectTranslation(
                        "Mini Bank", "Accounts and transfers.", null,
                        null, null, List.of("Test with real data."))),
                null, null));

        Project found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTranslations()).containsOnlyKeys("en");
        assertThat(found.getTranslations().get("en").title()).isEqualTo("Mini Bank");
        assertThat(found.getTranslations().get("en").learnings()).containsExactly("Test with real data.");
    }

    @Test
    void a_document_stored_before_translations_existed_reads_as_having_none() {
        mongoTemplate.save(ProjectMongoDocument.builder().slug("viejo").title("Viejo").build());

        assertThat(repository.findAll())
                .singleElement()
                .satisfies(p -> assertThat(p.getTranslations()).isEmpty());
    }

    private Project project(String slug) {
        return new Project(
                null, slug, "Titulo", "Resumen", "Descripcion",
                ProjectStatus.COMPLETADO, List.of(), null, null, null,
                List.of(), List.of(), List.of(), null,
                0, true, null, null, null, null, null);
    }
}
