package uk.gov.companieshouse.insolvency.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;

@Repository
public interface InsolvencyRepository extends MongoRepository<InsolvencyDocument, String> {

    InsolvencyDocument findByCompanyNumber(String companyNumber);
}
