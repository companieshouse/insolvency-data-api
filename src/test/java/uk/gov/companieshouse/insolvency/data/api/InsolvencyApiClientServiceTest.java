package uk.gov.companieshouse.insolvency.data.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;

@ExtendWith(MockitoExtension.class)
class InsolvencyApiClientServiceTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;

    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ApiResponse<Void> response;

    @InjectMocks
    private InsolvencyApiService insolvencyApiService;

    @Test
    void should_invoke_chs_kafka_endpoint_successfully() throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = insolvencyApiService.invokeChsKafkaApi(getInsolvencyDocument(), EventType.CHANGED);

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void should_invoke_chs_kafka_endpoint_delete_successfully() throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = insolvencyApiService.invokeChsKafkaApi(getInsolvencyDocument(), EventType.DELETED);

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_exception() throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> insolvencyApiService.invokeChsKafkaApi(getInsolvencyDocument(), EventType.CHANGED));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    void should_handle_exception_when_chs_kafka_endpoint_throws_appropriate_exception(int statusCode,
            String statusMessage, Class<Throwable> exception) throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(Mockito.any(), Mockito.any())).thenReturn(
                changedResourcePost);

        HttpResponseException.Builder builder = new HttpResponseException.Builder(statusCode,
                statusMessage, new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);

        assertThrows(exception,
                () -> insolvencyApiService.invokeChsKafkaApi(getInsolvencyDocument(), EventType.CHANGED));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(Mockito.any(),
                Mockito.any());
        verify(changedResourcePost, times(1)).execute();
    }

    private static Stream<Arguments> provideExceptionParameters() {
        return Stream.of(
                Arguments.of(503, "Service Unavailable", BadGatewayException.class),
                Arguments.of(405, "Method Not Allowed", BadGatewayException.class),
                Arguments.of(500, "Internal Service Error", RuntimeException.class)
        );
    }

    private InsolvencyDocument getInsolvencyDocument() {
        return new InsolvencyDocument("CH4000056", null, null,
                null, null);
    }
}
