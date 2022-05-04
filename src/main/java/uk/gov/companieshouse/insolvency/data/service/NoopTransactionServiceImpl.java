package uk.gov.companieshouse.insolvency.data.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
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
     *  @param logger               the logger
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
        boolean savedToDb = false;

        try {

            Optional<InsolvencyDocument> insolvencyDocumentFromDbOptional =
                    insolvencyRepository.findById(companyNumber);
            OffsetDateTime dateFromBodyRequest = companyInsolvency.getInternalData().getDeltaAt();

            if (insolvencyDocumentFromDbOptional.isPresent()) {
                InsolvencyDocument insolvencyDocumentFromDb =
                        insolvencyDocumentFromDbOptional.get();

                LocalDateTime deltaAtFromDbStr = insolvencyDocumentFromDb.getDeltaAt();

                if (deltaAtFromDbStr == null || dateFromBodyRequest.isAfter(
                        OffsetDateTime.of(deltaAtFromDbStr, ZoneOffset.UTC))) {
                    insolvencyDocument.setDeltaAt(dateFromBodyRequest.toLocalDateTime());
                    insolvencyDocument.setUpdatedAt(LocalDateTime.now());

                    insolvencyRepository.save(insolvencyDocument);
                    savedToDb = true;
                    logger.info(String.format("Company insolvency collection updated successfully "
                                    + "for company number %s", companyNumber));
                } else {
                    logger.info("Insolvency not persisted as the record provided is older"
                            + " than the one already stored.");
                }
            } else {
                insolvencyDocument.setDeltaAt(dateFromBodyRequest.toLocalDateTime());
                insolvencyDocument.setUpdatedAt(LocalDateTime.now());
                insolvencyRepository.save(insolvencyDocument);
                savedToDb = true;
                logger.info(String.format(
                        "Company insolvency collection inserted successfully for company number %s",
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

            logger.info(String.format("ChsKafka api invoked successfully for company number %s",
                    companyNumber));
        }
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

    @Override
    public void deleteInsolvency(String contextId, String companyNumber) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentOptional =
                    insolvencyRepository.findById(companyNumber);

            if (insolvencyDocumentOptional.isEmpty()) {
                throw new IllegalArgumentException(String.format(
                        "Company insolvency doesn't exist for company number %s",
                        companyNumber));
            }

            insolvencyRepository.deleteById(companyNumber);
            logger.info(String.format(
                    "Company insolvency delete called for company number %s",
                    companyNumber));

            insolvencyApiService.invokeChsKafkaApi(contextId, insolvencyDocumentOptional.get(),
                    EventType.DELETED);
            logger.info(String.format("ChsKafka api invoked successfully for company number %s",
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
                internalData.getDeltaAt().toLocalDateTime(),
                LocalDateTime.now(),
                internalData.getUpdatedBy());
    }

}
