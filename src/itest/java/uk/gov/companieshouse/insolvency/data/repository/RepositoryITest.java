package uk.gov.companieshouse.insolvency.data.repository;

import java.time.OffsetDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.config.AbstractMongoConfig;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.model.Updated;

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

    InsolvencyDocument insolvencyDocument = createInsolvencyDocument("CH253434");
    insolvencyRepository.save(insolvencyDocument);

    Assertions.assertThat(insolvencyRepository.findByCompanyNumber("CH253434")).isNotEmpty();
  }

  private InsolvencyDocument createInsolvencyDocument(String companyNumber) {
    InternalCompanyInsolvency companyInsolvency = new InternalCompanyInsolvency();

    InternalData internalData = new InternalData();
    internalData.setDeltaAt(OffsetDateTime.now());
    companyInsolvency.setInternalData(internalData);

    CompanyInsolvency externalData = new CompanyInsolvency();
    Updated updated = new Updated(internalData.getDeltaAt().toString(), internalData.getUpdatedBy(),
            "company-insolvency");
    companyInsolvency.setExternalData(externalData);

    externalData.setEtag(GenerateEtagUtil.generateEtag());
    return new InsolvencyDocument(companyNumber, externalData, updated);
  }

  @AfterAll
  static void tear(){
    mongoDBContainer.stop();
  }

}
