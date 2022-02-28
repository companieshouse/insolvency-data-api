package uk.gov.companieshouse.insolvency.data.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.AbstractMongoConfig;
import uk.gov.companieshouse.insolvency.data.InsolvencyRepository;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class RepositoryITest extends AbstractMongoConfig {

  @Autowired
  private InsolvencyRepository insolvencyRepository;

  @BeforeAll
  static void setup(){
    mongoDBContainer.start();
  }

  @Test
  void should_save_and_retrieve_insolvency_data() {
    InternalCompanyInsolvency insolvencyApi = new InternalCompanyInsolvency();
    insolvencyRepository.save(insolvencyApi);

    Assertions.assertThat(insolvencyRepository.findAll()).hasSize(1);
  }

  @AfterAll
  static void tear(){
    mongoDBContainer.stop();
  }

}
