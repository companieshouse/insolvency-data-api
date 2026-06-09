package uk.gov.companieshouse.insolvency.data;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.companieshouse.insolvency.data.config.AbstractMongoConfig;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/itest/resources/features",
        plugin = {"pretty", "json:target/cucumber-report.json"})
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DirtiesContext
@ActiveProfiles({"test"})
@ContextConfiguration(initializers = AbstractMongoConfig.Initializer.class)
public class CucumberFeaturesRunnerITest {

    @MockitoSpyBean
    InsolvencyRepository insolvencyRepository;
}
