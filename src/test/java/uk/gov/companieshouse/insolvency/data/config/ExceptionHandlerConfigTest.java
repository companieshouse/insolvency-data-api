package uk.gov.companieshouse.insolvency.data.config;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.controller.InsolvencyController;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentGoneException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerConfigTest {

    private static final String X_REQUEST_ID_VALUE = "b74566ce-da4e-41f9-bda5-2e672eff8733";

    private static final String X_REQUEST_ID = "x-request-id";

    private MockMvc mockMvc;

    @Mock
    private Logger logger;

    @InjectMocks
    private ExceptionHandlerConfig exceptionHandlerConfig;

    @Mock
    private WebRequest webRequest;

    @Mock
    private InsolvencyController insolvencyController;

    @Captor
    private ArgumentCaptor<Exception> exceptionCaptor;

    @Captor
    private ArgumentCaptor<String> contextCaptor;

    @Captor
    private ArgumentCaptor<String> errMsgCaptor;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws IllegalArgumentException {
        this.mockMvc =
                MockMvcBuilders
                        .standaloneSetup(insolvencyController)
                        .setControllerAdvice(exceptionHandlerConfig)
                        .build();

        doNothing().when(logger).errorContext(
                contextCaptor.capture(), errMsgCaptor.capture(), exceptionCaptor.capture(), any());
    }

    /**
     * Verifies the response exception status as well as whether the expected context-id,
     * message and exception itself have been passed to the logger.
     */
    @ParameterizedTest
    @MethodSource("provideExceptionParameters")
    void testHandleExceptionsUsingExceptionHandler(int expectedStatus, String expectedMsg,
                                                   Class<Throwable> exceptionClass) throws Exception {
        given(insolvencyController.insolvency(anyString(), anyString(), any()))
                .willAnswer(
                        invocation -> {
                            Constructor<Throwable> constr =
                                    exceptionClass.getDeclaredConstructor(String.class);
                            throw constr.newInstance("Error!");
                        }
                );

        verifyResponseStatus(performPutRequest(), expectedStatus);

        verify(logger).errorContext(
                contextCaptor.capture(), errMsgCaptor.capture(), exceptionCaptor.capture(), any());

        assertThat(exceptionCaptor.getValue(), instanceOf(exceptionClass));
    }

    private static Stream<Arguments> provideExceptionParameters() {
        return Stream.of(
                Arguments.of(400, "Bad request", BadRequestException.class),
                Arguments.of(400, "Bad request", HttpMessageNotReadableException.class),
                Arguments.of(405, "Unable to process the request, method not allowed",
                        MethodNotAllowedException.class),
                Arguments.of(410, "Resource gone", DocumentGoneException.class),
                Arguments.of(500, "Unexpected exception", RuntimeException.class),
                Arguments.of(500, "Unexpected exception", IllegalArgumentException.class),
                Arguments.of(503, "Service unavailable", ServiceUnavailableException.class)
        );
    }

    private void verifyResponseStatus(MockHttpServletResponse response, int expectedStatus) {
        Assertions.assertEquals(expectedStatus, response.getStatus());
    }

    private MockHttpServletResponse performPutRequest() throws Exception {
        InternalCompanyInsolvency companyInsolvency = new InternalCompanyInsolvency();

        return mockMvc.perform(MockMvcRequestBuilders
                        .put("/company/12345678/insolvency")
                        .header(X_REQUEST_ID, X_REQUEST_ID_VALUE)
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(companyInsolvency))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.log()).andReturn().getResponse();
    }
}