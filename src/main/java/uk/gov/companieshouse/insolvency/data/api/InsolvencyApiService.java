package uk.gov.companieshouse.insolvency.data.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.logging.Logger;

@Service
public class InsolvencyApiService {

    private static final String CHANGED_RESOURCE_URI = "/resource-changed";
    private final Logger logger;
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;
    private final ObjectMapper objectMapper;

    /**
     * Invoke Insolvency API.
     */
    public InsolvencyApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
                                ApiClientService apiClientService, ObjectMapper objectMapper,
                                Logger logger) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    /**
     * Call chs-kafka api.
     * @param insolvencyDocument company insolvency document
     * @return response returned from chs-kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(String contextId,
                                               InsolvencyDocument insolvencyDocument,
                                               String eventType,
                                               boolean isDelete) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(chsKafkaUrl);

        PrivateChangedResourcePost changedResourcePost = null;

        if (!isDelete) {
            changedResourcePost =
                    internalApiClient.privateChangedResourceHandler().postChangedResource(
                            CHANGED_RESOURCE_URI, mapChangedResource(
                                    contextId, insolvencyDocument, eventType,false)
                    );
        } else {
            changedResourcePost = internalApiClient.privateChangedResourceHandler()
                    .postChangedResource(
                            CHANGED_RESOURCE_URI, mapChangedResource(
                                    contextId, insolvencyDocument, eventType,true)
                    );
        }

        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            if (!statusCode.is2xxSuccessful() && statusCode != HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Unsuccessful call to /resource-changed endpoint", exp);
                throw new MethodNotAllowedException(exp.getMessage());
            } else if (statusCode == HttpStatus.SERVICE_UNAVAILABLE) {
                logger.error("Service unavailable while calling /resource-changed endpoint", exp);
                throw new ServiceUnavailableException(exp.getMessage());
            } else {
                logger.error("Error occurred while calling /resource-changed endpoint", exp);
                throw new RuntimeException(exp);
            }
        }
    }

    private ChangedResource mapChangedResource(String contextId,
                                               InsolvencyDocument insolvencyDocument,
                                               String eventType, boolean isDelete) {
        String resourceUri = "/company/" + insolvencyDocument.getId() + "/insolvency";

        ChangedResourceEvent event = new ChangedResourceEvent();
        event.setType(eventType);
        event.publishedAt(String.valueOf(OffsetDateTime.now()));

        ChangedResource changedResource = new ChangedResource();
        changedResource.setResourceUri(resourceUri);
        changedResource.event(event);
        changedResource.setResourceKind("company-insolvency");
        changedResource.setContextId(contextId);

        if (isDelete) {
            try {
                changedResource.setDeletedData(objectMapper.writeValueAsString(insolvencyDocument));
            } catch (JsonProcessingException exp) {
                logger.error("Error occurred while serializing to json", exp);
                throw new RuntimeException(exp);
            }
        }

        return changedResource;
    }

}
