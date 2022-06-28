package uk.gov.companieshouse.insolvency.data;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.insolvency.data.config.AbstractMongoConfig;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/itest/resources/features",
        plugin = {"pretty", "json:target/cucumber-report.json"})
@CucumberContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"test"})
public class CucumberFeaturesRunnerITest extends AbstractMongoConfig {

    public static void start() {
        mongoDBContainer.start();
    }

    public static void stop() {
        mongoDBContainer.stop();
    }

}
