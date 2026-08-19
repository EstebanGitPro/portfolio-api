package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.core.application.port.in.ProjectQueryUseCase;
import com.estebangitpro.portfolio.adapter.in.web.dto.ProjectResponse;
import com.estebangitpro.portfolio.adapter.in.web.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectPublicController {

    private final ProjectQueryUseCase projectQueryUseCase;
    private final ProjectMapper mapper;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllPublishedProjects() {
        return ResponseEntity.ok(projectQueryUseCase.getAllPublishedProjects()
                .stream()
                .map(mapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getPublishedProjectById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(projectQueryUseCase.getProjectById(id)));
    }
}
