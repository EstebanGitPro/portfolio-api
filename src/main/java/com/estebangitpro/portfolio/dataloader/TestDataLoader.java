package com.estebangitpro.portfolio.dataloader;

import com.estebangitpro.portfolio.model.Project;
import com.estebangitpro.portfolio.model.ProjectStatus;
import com.estebangitpro.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {

    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) throws Exception {

        Project project = Project.builder()
                .title("Mi Portfolio Web")
                .slug("mi-portfolio-web")
                .summary("Sitio web personal con React y Spring Boot")
                .description("Portfolio construido para mostrar mis proyectos como desarrollador")
                .status(ProjectStatus.COMPLETADO)
                .tags(List.of("React", "Spring Boot", "MongoDB"))
                .published(true)
                .build();

        projectRepository.save(project);

        System.out.println("Datos de prueba cargados: " + project.getTitle());

    }
}
