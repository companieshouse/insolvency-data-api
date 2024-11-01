package uk.gov.companieshouse.insolvency.data.service;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;
import static uk.gov.companieshouse.insolvency.data.util.DateTimeFormatter.isDeltaStale;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.ConflictException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentNotFoundException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class InsolvencyServiceImpl implements InsolvencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String RECOVERABLE_MONGO_EX_MSG = "Recoverable MongoDB exception";
    private static final String NONRECOVERABLE_MONGO_EX_MSG = "Failed to access MongoDB";

    private final InsolvencyRepository insolvencyRepository;
    private final InsolvencyApiService insolvencyApiService;

    /**
     * Insolvency service to store the insolvency data onto mongodb and call chs kafka endpoint.
     *
     * @param insolvencyRepository mongodb repository
     * @param insolvencyApiService chs-kafka api service
     */
    public InsolvencyServiceImpl(InsolvencyRepository insolvencyRepository,
            InsolvencyApiService insolvencyApiService) {
        this.insolvencyRepository = insolvencyRepository;
        this.insolvencyApiService = insolvencyApiService;
    }

    @Override
    public void processInsolvency(String companyNumber, InternalCompanyInsolvency companyInsolvency) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentFromDbOptional =
                    insolvencyRepository.findById(companyNumber);
            OffsetDateTime dateFromBodyRequest = companyInsolvency.getInternalData().getDeltaAt();

            insolvencyDocumentFromDbOptional
                    .ifPresent(existingDocument -> {
                        if (existingDocument.getDeltaAt() != null
                                && dateFromBodyRequest.isBefore(existingDocument.getDeltaAt())) {
                            LOGGER.error("Insolvency not persisted - stale delta at",
                                    DataMapHolder.getLogMap());
                            throw new ConflictException("Insolvency not persisted - stale delta at");
                        }
                    });

            InsolvencyDocument insolvencyDocument = mapInsolvencyDocument(
                    companyNumber, companyInsolvency);
            LOGGER.info("Successfully mapped insolvency document", DataMapHolder.getLogMap());

            insolvencyDocument.setDeltaAt(dateFromBodyRequest);
            insolvencyDocument.setUpdatedAt(LocalDateTime.now());

            insolvencyRepository.save(insolvencyDocument);
            LOGGER.info("Company insolvency successfully persisted in MongoDB", DataMapHolder.getLogMap());

            insolvencyApiService.invokeChsKafkaApi(companyNumber, insolvencyDocument.getCompanyInsolvency(),
                    EventType.CHANGED);
            LOGGER.info("ChsKafka api CHANGED invoked successfully", DataMapHolder.getLogMap());

        } catch (TransientDataAccessException ex) {
            LOGGER.info(RECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(RECOVERABLE_MONGO_EX_MSG, ex);
        } catch (DataAccessException ex) {
            LOGGER.error(NONRECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(NONRECOVERABLE_MONGO_EX_MSG, ex);
        }
    }

    @Override
    public CompanyInsolvency retrieveCompanyInsolvency(String companyNumber) {
        Optional<InsolvencyDocument> insolvencyDocumentOptional;
        try {
            insolvencyDocumentOptional =
                    insolvencyRepository.findById(companyNumber);
        } catch (TransientDataAccessException ex) {
            LOGGER.info(RECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(RECOVERABLE_MONGO_EX_MSG, ex);
        } catch (DataAccessException ex) {
            LOGGER.error(NONRECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(NONRECOVERABLE_MONGO_EX_MSG, ex);
        }

        InsolvencyDocument insolvencyDocument = insolvencyDocumentOptional.orElseGet(
                () -> {
                    LOGGER.info("Insolvency document not found", DataMapHolder.getLogMap());
                    throw new DocumentNotFoundException("Insolvency document not found");
                });

        LOGGER.info("Successfully retrieved insolvency document", DataMapHolder.getLogMap());
        return insolvencyDocument.getCompanyInsolvency();
    }

    @Override
    public void deleteInsolvency(String companyNumber, String deltaAt) {
        try {
            Optional<InsolvencyDocument> insolvencyDocumentOptional = insolvencyRepository.findById(companyNumber);

            if (insolvencyDocumentOptional.isEmpty()) {
                LOGGER.info("Invoking CHS Kafka API DELETED with empty deleted data", DataMapHolder.getLogMap());
                insolvencyApiService.invokeChsKafkaApi(companyNumber, null, EventType.DELETED);
            } else {
                InsolvencyDocument existingDocument = insolvencyDocumentOptional.get();
                if (isDeltaStale(deltaAt, existingDocument.getDeltaAt())) {
                    LOGGER.error("Insolvency not deleted - stale delta at", DataMapHolder.getLogMap());
                    throw new ConflictException("Insolvency not deleted - stale delta at");
                }

                LOGGER.info("Deleting company insolvency");
                insolvencyRepository.deleteById(companyNumber);

                LOGGER.info("Invoking CHS Kafka API DELETED", DataMapHolder.getLogMap());
                insolvencyApiService.invokeChsKafkaApi(companyNumber, existingDocument.getCompanyInsolvency(),
                        EventType.DELETED);
            }
        } catch (TransientDataAccessException ex) {
            LOGGER.info(RECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(RECOVERABLE_MONGO_EX_MSG, ex);
        } catch (DataAccessException ex) {
            LOGGER.error(NONRECOVERABLE_MONGO_EX_MSG, DataMapHolder.getLogMap());
            throw new BadGatewayException(NONRECOVERABLE_MONGO_EX_MSG, ex);
        }
    }

    private InsolvencyDocument mapInsolvencyDocument(String companyNumber,
            InternalCompanyInsolvency insolvencyApi) {
        InternalData internalData = insolvencyApi.getInternalData();
        CompanyInsolvency externalData = insolvencyApi.getExternalData();

        // Generating new Etag
        externalData.setEtag(GenerateEtagUtil.generateEtag());
        return new InsolvencyDocument(companyNumber,
                externalData,
                internalData.getDeltaAt(),
                LocalDateTime.now(),
                internalData.getUpdatedBy());
    }

}
