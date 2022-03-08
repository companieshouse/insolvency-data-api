package uk.gov.companieshouse.insolvency.data.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.InsolvencyRepository;
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
    public void saveInsolvency(String companyNumber, InternalCompanyInsolvency insolvencyApi) {
        insolvencyRepository.save(insolvencyApi);
    }

}
