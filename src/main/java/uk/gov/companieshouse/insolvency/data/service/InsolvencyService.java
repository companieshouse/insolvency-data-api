package uk.gov.companieshouse.insolvency.data.service;

import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

public interface InsolvencyService {

    /**
     * Processing company insolvency information.
     * 1. Saving company insolvency to mongodb collection
     * 2. call chs-kafka api endpoint
     * @param companyNumber company number
     * @param companyInsolvency company insolvency information {@link InternalCompanyInsolvency}
     */
    void processInsolvency(String companyNumber, InternalCompanyInsolvency companyInsolvency);

}
