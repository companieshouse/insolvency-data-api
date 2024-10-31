package uk.gov.companieshouse.insolvency.data.api;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.data.util.DateTimeFormatter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class InsolvencyApiService {

    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;

    /**
     * Invoke Insolvency API.
     */
    public InsolvencyApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
            ApiClientService apiClientService) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
    }

    /**
     * Call chs-kafka api.
     *
     * @param companyInsolvency company insolvency document
     * @return response returned from chs-kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(String companyNumber, CompanyInsolvency companyInsolvency,
            EventType eventType) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        internalApiClient.setBasePath(chsKafkaUrl);
        PrivateChangedResourcePost changedResourcePost = internalApiClient.privateChangedResourceHandler()
                .postChangedResource(CHANGED_RESOURCE_URI,
                        mapChangedResource(companyNumber, companyInsolvency, eventType));
        try {
            LOGGER.info("Calling CHS Kafka API", DataMapHolder.getLogMap());
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            final String msg = "Resource changed call failed and responded with: %d".formatted(ex.getStatusCode());
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        }
    }

    private ChangedResource mapChangedResource(String companyNumber, CompanyInsolvency insolvencyData,
            EventType eventType) {
        String resourceUri = "/company/" + companyNumber + "/insolvency";

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType(eventType.getEvent());
        event.publishedAt(DateTimeFormatter.formatPublishedAt(Instant.now()));

        ChangedResource changedResource = new ChangedResource();
        changedResource.setResourceUri(resourceUri);
        changedResource.event(event);
        changedResource.setResourceKind("company-insolvency");
        changedResource.setContextId(DataMapHolder.getRequestId());

        if (EventType.DELETED.equals(eventType)) {
            changedResource.setDeletedData(insolvencyData);
        }

        LOGGER.info("Successfully mapped ChangedResource object", DataMapHolder.getLogMap());
        return changedResource;
    }
}
