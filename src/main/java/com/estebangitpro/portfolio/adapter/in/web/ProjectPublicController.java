package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.core.application.port.in.ProjectQueryUseCase;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectResponse;
import com.estebangitpro.portfolio.adapter.in.web.mapper.ProjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Projects (public)", description = "Catálogo público, solo lectura")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectPublicController {

    private final ProjectQueryUseCase projectQueryUseCase;
    private final ProjectMapper mapper;

    @Operation(summary = "Listar proyectos publicados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyectos publicados, ordenados por order")
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllPublishedProjects() {
        return ResponseEntity.ok(projectQueryUseCase.getAllPublishedProjects()
                .stream()
                .map(mapper::toResponse)
                .toList());
    }

    @Operation(summary = "Obtener un proyecto publicado por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto encontrado"),
            @ApiResponse(responseCode = "404", description = "El proyecto no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getPublishedProjectById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(projectQueryUseCase.getProjectById(id)));
    }
}
