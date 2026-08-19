package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.domain.Project;
import com.estebangitpro.portfolio.core.application.port.out.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectMongoAdapter implements ProjectRepository {

    private final ProjectMongoRepository mongoRepository;

    @Override
    public List<Project> findAllPublished() {
        return mongoRepository.findByPublishedTrueOrderByOrderAsc()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> findAll() {
        return mongoRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Project> findById(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Project save(Project project) {
        ProjectMongoDocument doc = toDocument(project);
        ProjectMongoDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    private ProjectMongoDocument toDocument(Project domain) {
        return ProjectMongoDocument.builder()
                .id(domain.getId())
                .slug(domain.getSlug())
                .title(domain.getTitle())
                .summary(domain.getSummary())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .tags(domain.getTags())
                .thumbnail(domain.getThumbnail())
                .coverImage(domain.getCoverImage())
                .videoUrl(domain.getVideoUrl())
                .techStack(domain.getTechStack())
                .strategies(domain.getStrategies())
                .learnings(domain.getLearnings())
                .links(domain.getLinks())
                .order(domain.getOrder())
                .published(domain.isPublished())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Project toDomain(ProjectMongoDocument doc) {
        return new Project(
                doc.getId(), doc.getSlug(), doc.getTitle(), doc.getSummary(),
                doc.getDescription(), doc.getStatus(), doc.getTags(),
                doc.getThumbnail(), doc.getCoverImage(), doc.getVideoUrl(),
                doc.getTechStack(), doc.getStrategies(), doc.getLearnings(),
                doc.getLinks(), doc.getOrder(), doc.isPublished(),
                doc.getStartDate(), doc.getEndDate(),
                doc.getCreatedAt(), doc.getUpdatedAt()
        );
    }
}
