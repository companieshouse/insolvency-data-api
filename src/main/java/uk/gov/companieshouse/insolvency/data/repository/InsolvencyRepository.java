package uk.gov.companieshouse.insolvency.data.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;

@Repository
public interface InsolvencyRepository extends MongoRepository<InsolvencyDocument, String> {

    Optional<InsolvencyDocument> findByCompanyNumber(String companyNumber);
}
