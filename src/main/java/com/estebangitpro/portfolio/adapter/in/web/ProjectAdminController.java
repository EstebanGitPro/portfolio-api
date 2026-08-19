package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.core.application.port.in.ProjectCommandUseCase;
import com.estebangitpro.portfolio.core.application.port.in.ProjectQueryUseCase;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectRequest;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectResponse;
import com.estebangitpro.portfolio.adapter.in.web.mapper.ProjectMapper;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Projects (admin)", description = "Gestión de proyectos")
@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class ProjectAdminController {

    private final ProjectQueryUseCase projectQueryUseCase;
    private final ProjectCommandUseCase projectCommandUseCase;
    private final ProjectMapper mapper;

    @Operation(summary = "Crear un proyecto",
            description = "El slug se genera desde el title. id, createdAt y updatedAt los asigna el servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proyecto creado"),
            @ApiResponse(responseCode = "400", description = "Body invalido"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proyecto con ese slug")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(projectCommandUseCase.createProject(mapper.toDraft(requestDTO))));
    }

    @Operation(summary = "Listar todos los proyectos, incluidos los no publicados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todos los proyectos")
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectQueryUseCase.getAllProjectsIncludingUnpublished()
                .stream()
                .map(mapper::toResponse)
                .toList());
    }

    @Operation(summary = "Obtener un proyecto por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto encontrado"),
            @ApiResponse(responseCode = "404", description = "El proyecto no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(projectQueryUseCase.getProjectById(id)));
    }

    @Operation(summary = "Actualizar un proyecto",
            description = "Reemplaza el proyecto completo. createdAt se preserva; updatedAt se refresca.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto actualizado"),
            @ApiResponse(responseCode = "400", description = "Body invalido"),
            @ApiResponse(responseCode = "404", description = "El proyecto no existe"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proyecto con ese slug")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String id,
                                                             @Valid @RequestBody ProjectRequest requestDTO) {
        return ResponseEntity.ok(
                mapper.toResponse(projectCommandUseCase.updateProject(id, mapper.toDraft(requestDTO))));
    }

    @Operation(summary = "Eliminar un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "El proyecto no existe")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectCommandUseCase.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
