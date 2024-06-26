package uk.gov.companieshouse.insolvency.data.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentNotFoundException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class InsolvencyServiceImpl implements InsolvencyService {

    private final Logger logger;
    private final InsolvencyRepository insolvencyRepository;
    private final InsolvencyApiService insolvencyApiService;

    /**
     * Insolvency service to store the insolvency data onto mongodb and call chs kafka endpoint.
     *
     * @param logger               the logger
     * @param insolvencyRepository mongodb repository
     * @param insolvencyApiService chs-kafka api service
     */
    public InsolvencyServiceImpl(Logger logger, InsolvencyRepository insolvencyRepository,
                                 InsolvencyApiService insolvencyApiService) {
        this.logger = logger;
        this.insolvencyRepository = insolvencyRepository;
        this.insolvencyApiService = insolvencyApiService;
    }

    @Override
    public void processInsolvency(String contextId, String companyNumber, InternalCompanyInsolvency companyInsolvency) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentFromDbOptional =
                    insolvencyRepository.findById(companyNumber);
            OffsetDateTime dateFromBodyRequest = companyInsolvency.getInternalData().getDeltaAt();

            insolvencyDocumentFromDbOptional
                    .ifPresent(existingDocument -> {
                                if (!dateFromBodyRequest.isAfter(existingDocument.getDeltaAt())) {
                                    logger.info("Insolvency not persisted as the record provided is older"
                                            + " than the one already stored.");
                                    throw new IllegalArgumentException("Stale delta at");
                                }
                            }
                    );

            InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(
                    companyNumber, companyInsolvency);

            insolvencyDocument.setDeltaAt(dateFromBodyRequest);
            insolvencyDocument.setUpdatedAt(LocalDateTime.now());

            insolvencyApiService.invokeChsKafkaApi(contextId, insolvencyDocument,
                    EventType.CHANGED);
            logger.info(String.format(
                    "ChsKafka api CHANGED invoked successfully for context id %s and company number %s",
                    contextId,
                    companyNumber));

            insolvencyRepository.save(insolvencyDocument);
            logger.info(String.format(
                    "Company insolvency successfully upserted in MongoDB with context id %s and company number %s",
                    contextId,
                    companyNumber));

        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        } catch (DataAccessException dbException) {
            throw new ServiceUnavailableException(dbException.getMessage());
        }
    }

    @Override
    public CompanyInsolvency retrieveCompanyInsolvency(String companyNumber) {
        Optional<InsolvencyDocument> insolvencyDocumentOptional =
                insolvencyRepository.findById(companyNumber);

        InsolvencyDocument insolvencyDocument = insolvencyDocumentOptional.orElseThrow(
                () -> new DocumentNotFoundException(String.format(
                        "Resource not found for company number: %s", companyNumber)));

        return insolvencyDocument.getCompanyInsolvency();
    }

    @Override
    public void deleteInsolvency(String contextId, String companyNumber) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentOptional =
                    insolvencyRepository.findById(companyNumber);

            if (insolvencyDocumentOptional.isEmpty()) {
                throw new DocumentNotFoundException(String.format(
                        "Company insolvency doesn't exist for company number %s",
                        companyNumber));
            }

            insolvencyApiService.invokeChsKafkaApi(contextId, insolvencyDocumentOptional.get(),
                    EventType.DELETED);
            logger.info(String.format("ChsKafka api DELETED "
                            + "invoked successfully for context id %s and company number %s",
                    contextId,
                    companyNumber));

            insolvencyRepository.deleteById(companyNumber);
            logger.info(String.format(
                    "Company insolvency is deleted in "
                            + "MongoDB with context id %s and company number %s",
                    contextId,
                    companyNumber));
        } catch (DataAccessException dbException) {
            throw new ServiceUnavailableException(dbException.getMessage());
        }
    }

    private InsolvencyDocument mapInsolvencyDocument(String companyNumber,
                                                     InternalCompanyInsolvency insolvencyApi) {
        InternalData internalData = insolvencyApi.getInternalData();
        CompanyInsolvency externalData = insolvencyApi.getExternalData();

        //Generating new Etag
        externalData.setEtag(GenerateEtagUtil.generateEtag());
        return new InsolvencyDocument(companyNumber,
                externalData,
                internalData.getDeltaAt(),
                LocalDateTime.now(),
                internalData.getUpdatedBy());
    }

}
