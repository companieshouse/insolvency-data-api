package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.config.ExceptionHandlerConfig;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyServiceImpl;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = InsolvencyController.class)
@ContextConfiguration(classes = {InsolvencyController.class, ExceptionHandlerConfig.class})
class InsolvencyControllerTest {
    private static final String COMPANY_NUMBER = "02588581";
    private static final String URL = String.format("/company/%s/insolvency", COMPANY_NUMBER);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Logger logger;

    @MockBean
    private InsolvencyServiceImpl insolvencyService;

    private ObjectMapper mapper = new ObjectMapper();

    private Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @DisplayName("Insolvency PUT request")
    public void callInsolvencyPutRequest() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doNothing().when(insolvencyService).processInsolvency(anyString(), anyString(),
                isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insolvency PUT request - IllegalArgumentException status code 404 not found")
    public void callInsolvencyPutRequestIllegalArgument() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new IllegalArgumentException())
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insolvency PUT request - BadRequestException status code 400")
    public void callInsolvencyPutRequestBadRequest() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Insolvency PUT request - MethodNotAllowed status code 405")
    public void callInsolvencyPutRequestMethodNotAllowed() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", URL)))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Insolvency PUT request - InternalServerError status code 500")
    public void callInsolvencyPutRequestInternalServerError() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insolvency PUT request - ServiceUnavailable status code 503")
    public void callInsolvencyPutRequestServiceUnavailable() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .content(gson.toJson(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Insolvency DELETE request")
    void callInsolvencyDeleteRequest() throws Exception {
        doNothing().when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insolvency DELETE request - IllegalArgumentException status code 404 not found")
    void callInsolvencyDeleteRequestIllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException())
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insolvency DELETE request - BadRequestException status code 400")
    void callInsolvencyDeleteRequestBadRequest() throws Exception {
        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Insolvency DELETE request - MethodNotAllowed status code 405")
    void callInsolvencyDeleteRequestMethodNotAllowed() throws Exception {
        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", URL)))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Insolvency DELETE request - InternalServerError status code 500")
    void callInsolvencyDeleteRequestInternalServerError() throws Exception {

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insolvency DELETE request - ServiceUnavailable status code 503")
    void callInsolvencyDeleteRequestServiceUnavailable() throws Exception {

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342"))
                .andExpect(status().isServiceUnavailable());
    }
}
