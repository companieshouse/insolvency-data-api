package uk.gov.companieshouse.insolvency.data.service;

import java.util.List;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.model.InternalCompanyInsolvency2;
import uk.gov.companieshouse.insolvency.data.model.Updated;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class InsolvencyService {

    private final Logger logger;
    private final InsolvencyRepository insolvencyRepository;

    public InsolvencyService(Logger logger, InsolvencyRepository insolvencyRepository) {
        this.logger = logger;
        this.insolvencyRepository = insolvencyRepository;
    }

    /**
     * Persist company insolvency information to mongodb collection.
     * @param insolvencyApi company insolvency information {@link InternalCompanyInsolvency}
     */
    public void saveInsolvency(String companyNumber, InternalCompanyInsolvency2 insolvencyApi) {
        InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(companyNumber, insolvencyApi);
        insolvencyRepository.save(insolvencyDocument);

        List<InsolvencyDocument> all = insolvencyRepository.findAll();
        System.out.println("Data Fetched" + all);
    }

    private InsolvencyDocument mapInsolvencyDocument(String companyNumber,
                                                     InternalCompanyInsolvency2 insolvencyApi) {
        InternalData internalData = insolvencyApi.getInternalData();
        CompanyInsolvency externalData = insolvencyApi.getExternalData();
        Updated updated = new Updated(internalData.getDeltaAt().toString(),
                internalData.getUpdatedBy(), "company-insolvency");

        return new InsolvencyDocument(companyNumber, externalData, updated);
    }

}
