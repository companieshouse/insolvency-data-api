package uk.gov.companieshouse.insolvency.data;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.insolvency.data.config.AbstractMongoConfig;

@Suite
@SelectClasspathResource("features")
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"test"})
public class CucumberFeaturesRunnerITest extends AbstractMongoConfig { // NOSONAR

}
