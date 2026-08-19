package com.estebangitpro.portfolio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Describes the API for the generated OpenAPI document.
 *
 * <p>The document itself is derived from the controllers and DTOs, so it cannot drift
 * from the code. Only what the code cannot express — title, prose, servers — lives here.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio API")
                        .version("1.0.0")
                        .description("""
                                API del portfolio personal de Esteban. Expone un catálogo público \
                                de proyectos, un área de administración para gestionarlos y un \
                                módulo de analítica de visitas.

                                Construida con arquitectura hexagonal: el dominio no conoce Spring, \
                                MongoDB ni HTTP.

                                **Slugs**: se derivan del título y son únicos. Crear un proyecto cuyo \
                                título genere un slug ya existente devuelve `409 Conflict`."""))
                .servers(List.of(new Server()
                        .url("http://localhost:8086")
                        .description("Local")));
    }
}
