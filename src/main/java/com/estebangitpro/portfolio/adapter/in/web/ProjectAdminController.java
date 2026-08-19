package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.core.application.port.in.ProjectCommandUseCase;
import com.estebangitpro.portfolio.core.application.port.in.ProjectQueryUseCase;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectRequest;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectResponse;
import com.estebangitpro.portfolio.adapter.in.web.mapper.ProjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class ProjectAdminController {

    private final ProjectQueryUseCase projectQueryUseCase;
    private final ProjectCommandUseCase projectCommandUseCase;
    private final ProjectMapper mapper;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(projectCommandUseCase.createProject(mapper.toDraft(requestDTO))));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectQueryUseCase.getAllProjectsIncludingUnpublished()
                .stream()
                .map(mapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(projectQueryUseCase.getProjectById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String id,
                                                             @Valid @RequestBody ProjectRequest requestDTO) {
        return ResponseEntity.ok(
                mapper.toResponse(projectCommandUseCase.updateProject(id, mapper.toDraft(requestDTO))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectCommandUseCase.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
