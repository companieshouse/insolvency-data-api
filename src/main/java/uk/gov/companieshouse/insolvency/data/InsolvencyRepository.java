package uk.gov.companieshouse.insolvency.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

public interface InsolvencyRepository extends MongoRepository<InternalCompanyInsolvency, String> {
}
