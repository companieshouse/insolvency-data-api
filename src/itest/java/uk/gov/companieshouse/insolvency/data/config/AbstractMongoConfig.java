package uk.gov.companieshouse.insolvency.data.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Mongodb configuration runs on test container.
 */
public class AbstractMongoConfig {

    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:8.2.5"));


    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.setWaitStrategy(Wait.defaultWaitStrategy()
            .withStartupTimeout(Duration.of(300, SECONDS)));

        mongoDBContainer.start();

        registry.add("spring.mongodb.uri", (() -> mongoDBContainer.getReplicaSetUrl() +
            "?serverSelectionTimeoutMS=100&connectTimeoutMS=100"));
    }
}
