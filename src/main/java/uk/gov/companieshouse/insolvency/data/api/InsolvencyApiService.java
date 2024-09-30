package uk.gov.companieshouse.insolvency.data.api;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
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
     * @param insolvencyDocument company insolvency document
     * @return response returned from chs-kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(String contextId,
            InsolvencyDocument insolvencyDocument,
            EventType eventType) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(chsKafkaUrl);
        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapChangedResource(
                                contextId, insolvencyDocument, eventType)
                );
        try {
            LOGGER.info("Calling CHS Kafka API", DataMapHolder.getLogMap());
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            DataMapHolder.get().status(Integer.toString(ex.getStatusCode()));
            final String msg = "Resource changed call failed and responded with: %d".formatted(ex.getStatusCode());
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new BadGatewayException(msg, ex);
        }
    }

    private ChangedResource mapChangedResource(String contextId,
            InsolvencyDocument insolvencyDocument,
            EventType eventType) {
        String resourceUri = "/company/" + insolvencyDocument.getId() + "/insolvency";

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType(eventType.getEvent());
        event.publishedAt(DateTimeFormatter.formatPublishedAt(Instant.now()));

        ChangedResource changedResource = new ChangedResource();
        changedResource.setResourceUri(resourceUri);
        changedResource.event(event);
        changedResource.setResourceKind("company-insolvency");
        changedResource.setContextId(contextId);

        if (EventType.DELETED.equals(eventType)) {
            changedResource.setDeletedData(insolvencyDocument.getCompanyInsolvency());
        }

        LOGGER.info("Successfully mapped ChangedResource object", DataMapHolder.getLogMap());
        return changedResource;
    }

}
