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

    public void persist(final InternalCompanyInsolvency insolvencyApi) {
        logger.debug(String.format("Data saved in company_insolvency collection : %s", insolvencyApi.toString()));
        insolvencyRepository.save(insolvencyApi);
    }
}
