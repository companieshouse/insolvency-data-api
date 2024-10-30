package uk.gov.companieshouse.insolvency.data.service;

import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

public interface InsolvencyService {

    /**
     * Processing company insolvency information.
     * 1. Saving company insolvency to mongodb collection
     * 2. call chs-kafka api endpoint
     *
     * @param contextId         the contextId for correlation
     * @param companyNumber     company number
     * @param companyInsolvency company insolvency information {@link InternalCompanyInsolvency}
     */
    void processInsolvency(String contextId, String companyNumber,
            InternalCompanyInsolvency companyInsolvency);

    CompanyInsolvency retrieveCompanyInsolvency(String companyNumber);

    void deleteInsolvency(String companyNumber, String deltaAt);
}
