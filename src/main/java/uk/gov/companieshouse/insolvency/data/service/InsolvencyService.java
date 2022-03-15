package uk.gov.companieshouse.insolvency.data.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.model.Updated;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

@Service
public class InsolvencyService {

    private final InsolvencyRepository insolvencyRepository;

    /**
     * Insolvency service to store insolvency data onto mongodb and call chs kafka endpoint.
     * @param insolvencyRepository repository to save data to db
     */
    public InsolvencyService(InsolvencyRepository insolvencyRepository) {
        this.insolvencyRepository = insolvencyRepository;

    }

    /**
     * Persist company insolvency information to mongodb collection.
     * @param insolvencyApi company insolvency information {@link InternalCompanyInsolvency}
     */
    public void saveInsolvency(String companyNumber, InternalCompanyInsolvency insolvencyApi) {
        InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(companyNumber, insolvencyApi);

        insolvencyRepository.save(insolvencyDocument);
    }

    private InsolvencyDocument mapInsolvencyDocument(String companyNumber,
                                                     InternalCompanyInsolvency insolvencyApi) {
        InternalData internalData = insolvencyApi.getInternalData();
        CompanyInsolvency externalData = insolvencyApi.getExternalData();
        Updated updated = new Updated(internalData.getDeltaAt().toString(),
                internalData.getUpdatedBy(), "company-insolvency");

        return new InsolvencyDocument(companyNumber, externalData, updated);
    }

}
