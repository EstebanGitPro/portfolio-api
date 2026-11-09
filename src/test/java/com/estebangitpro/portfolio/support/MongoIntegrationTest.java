package com.estebangitpro.portfolio.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for tests that need a real MongoDB.
 *
 * <p>Starts one throwaway container for the whole test run (singleton pattern: the container
 * is started once in a static initializer and reclaimed by Ryuk when the JVM exits), so every
 * test class shares it without paying the startup cost repeatedly.
 *
 * <p>Properties are injected as {@code host}/{@code port} rather than through
 * {@code @ServiceConnection} because {@code MongoConfig} builds the {@code MongoClient} from
 * those two properties and never reads {@code spring.data.mongodb.uri}.
 */
public abstract class MongoIntegrationTest {

    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:7.0.4");
    private static final int MONGO_PORT = 27017;

    protected static final MongoDBContainer MONGO = new MongoDBContainer(MONGO_IMAGE);

    static {
        MONGO.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", MONGO::getHost);
        registry.add("spring.data.mongodb.port", () -> MONGO.getMappedPort(MONGO_PORT));
        registry.add("spring.data.mongodb.database", () -> "portfolio-test");
        registry.add("spring.data.mongodb.username", () -> "");
        registry.add("spring.data.mongodb.password", () -> "");
    }
}
