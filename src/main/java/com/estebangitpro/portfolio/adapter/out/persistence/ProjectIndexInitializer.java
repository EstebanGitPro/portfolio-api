package com.estebangitpro.portfolio.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Creates the unique index that keeps project slugs distinct.
 *
 * <p>Slugs address projects in public URLs, so duplicates would make a project
 * unreachable. Enforcing it in the database means the guarantee holds even for writes
 * that do not go through the application.
 *
 * <p>Declared programmatically rather than with {@code @Indexed} because automatic index
 * creation is disabled by default in Spring Data MongoDB.
 */
@Component
@RequiredArgsConstructor
public class ProjectIndexInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        Index uniqueSlug = new Index("slug", Sort.Direction.ASC).unique();
        mongoTemplate.indexOps(ProjectMongoDocument.class).createIndex(uniqueSlug);
    }
}
