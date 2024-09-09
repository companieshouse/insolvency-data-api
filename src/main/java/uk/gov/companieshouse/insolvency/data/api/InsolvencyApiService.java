package uk.gov.companieshouse.insolvency.data.api;

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
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.util.DateTimeFormatter;
import uk.gov.companieshouse.logging.Logger;

@Service
public class InsolvencyApiService {

    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";

    private final Logger logger;
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;

    /**
     * Invoke Insolvency API.
     */
    public InsolvencyApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
            ApiClientService apiClientService,
            Logger logger) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.logger = logger;
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
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            if (!statusCode.is2xxSuccessful() && statusCode != HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Unsuccessful call to /private/resource-changed endpoint", exp);
                throw new MethodNotAllowedException(exp.getMessage());
            } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Service unavailable while calling /private/resource-changed endpoint", exp);
                throw new ServiceUnavailableException(exp.getMessage());
            } else {
                logger.error("Error occurred while calling /private/resource-changed endpoint", exp);
                throw new RuntimeException(exp);
            }
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

        return changedResource;
    }

}
