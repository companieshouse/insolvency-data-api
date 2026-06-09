package uk.gov.companieshouse.insolvency.data.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Mongodb configuration runs on test container.
 */
public class AbstractMongoConfig {

    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:5.0.12"));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext context) {
            mongoDBContainer.start();
            TestPropertyValues.of(
                    "spring.mongodb.uri=" + mongoDBContainer.getReplicaSetUrl()
            ).applyTo(context);
        }
    }
}
