package uk.gov.companieshouse.insolvency.data.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.SerDesException;

@ExtendWith(MockitoExtension.class)
class InsolvencyApiClientServiceTest {

    private static final String COMPANY_NUMBER = "CH4000056";
    private static final String SERIALISED_DATA = "serialisedData";
    private static final Object MAPPED_DELETED_DATA = new Object();

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ObjectMapper objectMapper;

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
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);

        ApiResponse<?> apiResponse = insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, new CompanyInsolvency(),
                EventType.CHANGED);

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(),
                any());
        verify(changedResourcePost, times(1)).execute();
    }

    @Test
    void should_invoke_chs_kafka_endpoint_delete_successfully() throws Exception {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(objectMapper.writeValueAsString(any())).thenReturn(SERIALISED_DATA);
        when(objectMapper.readValue(anyString(), eq(Object.class))).thenReturn(MAPPED_DELETED_DATA);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        CompanyInsolvency companyInsolvency = new CompanyInsolvency();

        ApiResponse<?> apiResponse = insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, companyInsolvency,
                EventType.DELETED);

        Assertions.assertThat(apiResponse).isNotNull();

        verify(apiClientService).getInternalApiClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(objectMapper).writeValueAsString(companyInsolvency);
        verify(objectMapper).readValue(SERIALISED_DATA, Object.class);
        verify(privateChangedResourceHandler).postChangedResource(any(), any());
        verify(changedResourcePost).execute();
    }

    @Test
    void should_handle_exception_when_chs_kafka_endpoint_throws_exception() throws ApiErrorResponseException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(
                changedResourcePost);
        when(changedResourcePost.execute()).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
                () -> insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, new CompanyInsolvency(),
                        EventType.CHANGED));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(),
                any());
        verify(changedResourcePost, times(1)).execute();
    }

    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    void should_handle_exception_when_chs_kafka_endpoint_throws_appropriate_exception(int statusCode,
            String statusMessage, Class<Throwable> exception) throws ApiErrorResponseException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(
                changedResourcePost);

        HttpResponseException.Builder builder = new HttpResponseException.Builder(statusCode,
                statusMessage, new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException =
                new ApiErrorResponseException(builder);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);

        assertThrows(exception,
                () -> insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, new CompanyInsolvency(),
                        EventType.CHANGED));

        verify(apiClientService, times(1)).getInternalApiClient();
        verify(internalApiClient, times(1)).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(1)).postChangedResource(any(),
                any());
        verify(changedResourcePost, times(1)).execute();
    }

    private static Stream<Arguments> provideExceptionParameters() {
        return Stream.of(
                Arguments.of(503, "Service Unavailable", BadGatewayException.class),
                Arguments.of(405, "Method Not Allowed", BadGatewayException.class),
                Arguments.of(500, "Internal Service Error", RuntimeException.class)
        );
    }

    @Test
    void shouldThrowInternalServerErrorWhenObjectMapperFailsReadCallDuringDelete() throws Exception {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(objectMapper.writeValueAsString(any())).thenReturn(SERIALISED_DATA);
        when(objectMapper.readValue(anyString(), eq(Object.class))).thenThrow(JsonProcessingException.class);

        Executable actual = () -> insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, new CompanyInsolvency(),
                EventType.DELETED);

        assertThrows(SerDesException.class, actual);
        verify(apiClientService).getInternalApiClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(0)).postChangedResource(any(),
                any());
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }

    @Test
    void shouldThrowInternalServerErrorWhenObjectMapperFailsWriteCallDuringDelete() throws Exception {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        Executable actual = () -> insolvencyApiService.invokeChsKafkaApi(COMPANY_NUMBER, new CompanyInsolvency(),
                EventType.DELETED);

        assertThrows(SerDesException.class, actual);
        verify(apiClientService).getInternalApiClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler, times(0)).postChangedResource(any(),
                any());
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }
}
