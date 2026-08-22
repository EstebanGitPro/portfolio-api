package com.estebangitpro.portfolio.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Creates the TTL index that expires analytics documents after the retention period.
 *
 * <p>Index management is a persistence detail, so it lives with the adapter that owns
 * the collection rather than in the application's configuration.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsIndexInitializer implements CommandLineRunner {

    private static final int RETENTION_DAYS = 90;

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        Index createdAtTtl = new Index("createdAt", Sort.Direction.ASC)
                .expire(RETENTION_DAYS, TimeUnit.DAYS);
        mongoTemplate.indexOps(AnalyticsMongoDocument.class).ensureIndex(createdAtTtl);
    }
}
