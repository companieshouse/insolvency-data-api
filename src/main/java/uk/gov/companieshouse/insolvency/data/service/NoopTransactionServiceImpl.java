package uk.gov.companieshouse.insolvency.data.service;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.model.Updated;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
@ConditionalOnProperty(
        value = "insolvency.transactions", havingValue = "false", matchIfMissing = true)
public class NoopTransactionServiceImpl implements InsolvencyService {

    private final Logger logger;
    private final InsolvencyRepository insolvencyRepository;
    private final InsolvencyApiService insolvencyApiService;

    /**
     * Insolvency service to store insolvency data onto mongodb and call chs kafka endpoint.
     *
     * @param logger               the logger
     * @param insolvencyRepository mongodb repository
     * @param insolvencyApiService chs-kafka api service
     */
    public NoopTransactionServiceImpl(Logger logger, InsolvencyRepository insolvencyRepository,
                                      InsolvencyApiService insolvencyApiService) {
        this.logger = logger;
        this.insolvencyRepository = insolvencyRepository;
        this.insolvencyApiService = insolvencyApiService;
    }

    @Override
    public void processInsolvency(String contextId, String companyNumber,
                                  InternalCompanyInsolvency companyInsolvency) {
        InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(
                companyNumber, companyInsolvency);

        try {
            insolvencyRepository.save(insolvencyDocument);
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        } catch (DataAccessException dbException) {
            throw new ServiceUnavailableException(dbException.getMessage());
        }

        logger.info(String.format(
                "Company insolvency collection updated successfully for company number %s",
                companyNumber));

        insolvencyApiService.invokeChsKafkaApi(contextId, companyNumber);

        logger.info(String.format("ChsKafka api invoked successfully for company number %s",
                companyNumber));
    }

    @Override
    public CompanyInsolvency retrieveCompanyInsolvency(String companyNumber) {
        Optional<InsolvencyDocument> insolvencyDocumentOptional =
                insolvencyRepository.findById(companyNumber);

        InsolvencyDocument insolvencyDocument = insolvencyDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        "Resource not found for company number: %s", companyNumber)));

        return insolvencyDocument.getCompanyInsolvency();
    }

    private InsolvencyDocument mapInsolvencyDocument(String companyNumber,
                                                     InternalCompanyInsolvency insolvencyApi) {
        InternalData internalData = insolvencyApi.getInternalData();
        CompanyInsolvency externalData = insolvencyApi.getExternalData();
        Updated updated = new Updated(internalData.getDeltaAt().toString(),
                internalData.getUpdatedBy(), "company-insolvency");

        //Generating new Etag
        externalData.setEtag(GenerateEtagUtil.generateEtag());
        return new InsolvencyDocument(companyNumber, externalData, updated);
    }

}
