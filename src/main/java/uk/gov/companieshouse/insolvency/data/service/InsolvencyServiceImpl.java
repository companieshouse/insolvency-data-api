package uk.gov.companieshouse.insolvency.data.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentGoneException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
@Primary
@ConditionalOnProperty(
        value = "insolvency.transactions", havingValue = "true")
public class InsolvencyServiceImpl implements InsolvencyService {

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
    public InsolvencyServiceImpl(Logger logger, InsolvencyRepository insolvencyRepository,
                                 InsolvencyApiService insolvencyApiService) {
        this.logger = logger;
        this.insolvencyRepository = insolvencyRepository;
        this.insolvencyApiService = insolvencyApiService;
    }

    @Override
    @Transactional
    public void processInsolvency(String contextId, String companyNumber,
                                  InternalCompanyInsolvency companyInsolvency) {
        InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(
                companyNumber, companyInsolvency);
        boolean savedToDb = false;

        try {

            Optional<InsolvencyDocument> insolvencyDocumentFromDbOptional =
                    insolvencyRepository.findById(companyNumber);
            OffsetDateTime dateFromBodyRequest = companyInsolvency.getInternalData().getDeltaAt();

            if (insolvencyDocumentFromDbOptional.isPresent()) {
                InsolvencyDocument insolvencyDocumentFromDb =
                        insolvencyDocumentFromDbOptional.get();

                OffsetDateTime deltaAtFromDbStr = insolvencyDocumentFromDb.getDeltaAt();

                if (deltaAtFromDbStr == null || dateFromBodyRequest.isAfter(
                        deltaAtFromDbStr)) {
                    insolvencyDocument.setDeltaAt(dateFromBodyRequest);
                    insolvencyDocument.setUpdatedAt(LocalDateTime.now());
                    insolvencyDocument.getCompanyInsolvency().setStatus(null);

                    var statusFromDb = Optional.ofNullable(
                                    insolvencyDocumentFromDb.getCompanyInsolvency())
                            .map(CompanyInsolvency::getStatus);

                    if (statusFromDb.isPresent() && !statusFromDb.get().isEmpty()) {
                        insolvencyDocument.getCompanyInsolvency().setStatus(statusFromDb.get());
                    }

                    insolvencyRepository.save(insolvencyDocument);
                    savedToDb = true;
                    logger.info(String.format(
                            "Company insolvency is updated in MongoDB with "
                                    + "context id %s and company number %s",
                            contextId,
                            companyNumber));
                } else {
                    logger.info(String.format("Insolvency not persisted as "
                                    + "the record provided is older"
                            + " than the one already stored, context id is %s "
                                    + "and company number %s",
                            contextId,
                            companyNumber));
                }
            } else {
                insolvencyDocument.setDeltaAt(dateFromBodyRequest);
                insolvencyDocument.setUpdatedAt(LocalDateTime.now());
                insolvencyDocument.getCompanyInsolvency().setStatus(null);
                insolvencyRepository.save(insolvencyDocument);
                savedToDb = true;
                logger.info(String.format(
                        "Company insolvency is inserted in MongoDB with "
                                + "context id %s and company number %s",
                        contextId,
                        companyNumber));
            }
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        } catch (DataAccessException dbException) {
            throw new ServiceUnavailableException(dbException.getMessage());
        }

        if (savedToDb) {
            insolvencyApiService.invokeChsKafkaApi(contextId, insolvencyDocument,
                    EventType.CHANGED);

            logger.info(String.format("ChsKafka api CHANGED invoked "
                            + "successfully for context id %s and company number %s",
                    contextId,
                    companyNumber));
        }
    }

    @Override
    public CompanyInsolvency retrieveCompanyInsolvency(String companyNumber) {
        Optional<InsolvencyDocument> insolvencyDocumentOptional =
                insolvencyRepository.findById(companyNumber);

        InsolvencyDocument insolvencyDocument = insolvencyDocumentOptional.orElseThrow(
                () -> new DocumentGoneException(String.format(
                        "Resource not found for company number: %s", companyNumber)));

        return insolvencyDocument.getCompanyInsolvency();
    }

    @Override
    public void deleteInsolvency(String contextId, String companyNumber) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentOptional =
                    insolvencyRepository.findById(companyNumber);

            if (insolvencyDocumentOptional.isEmpty()) {
                throw new DocumentGoneException(String.format(
                        "Company insolvency doesn't exist for company number %s",
                        companyNumber));
            }

            insolvencyRepository.deleteById(companyNumber);
            logger.info(String.format(
                    "Company insolvency is deleted in MongoDB with "
                            + "context id %s and company number %s",
                    contextId,
                    companyNumber));

            insolvencyApiService.invokeChsKafkaApi(contextId, insolvencyDocumentOptional.get(),
                    EventType.DELETED);
            logger.info(String.format("ChsKafka api DELETE invoked successfully "
                            + "for context id %s and company number %s",
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

        //Generating the new Etag
        externalData.setEtag(GenerateEtagUtil.generateEtag());
        return new InsolvencyDocument(companyNumber,
                externalData,
                internalData.getDeltaAt(),
                LocalDateTime.now(),
                internalData.getUpdatedBy());
    }

}
