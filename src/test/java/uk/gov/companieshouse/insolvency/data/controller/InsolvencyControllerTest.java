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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.config.ExceptionHandlerConfig;
import uk.gov.companieshouse.insolvency.data.config.WebSecurityConfig;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentNotFoundException;
import uk.gov.companieshouse.insolvency.data.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyServiceImpl;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = InsolvencyController.class)
@ContextConfiguration(classes = {InsolvencyController.class, ExceptionHandlerConfig.class})
@Import({WebSecurityConfig.class})
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
    void callInsolvencyPutRequest() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doNothing().when(insolvencyService).processInsolvency(anyString(), anyString(),
                isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insolvency PUT request - DocumentGoneException status code 410 gone")
    void callInsolvencyPutRequestDocumentGone() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new DocumentNotFoundException("Document not found"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isGone());
    }

    @Test
    @DisplayName("Insolvency PUT request - BadRequestException status code 400")
    void callInsolvencyPutRequestBadRequest() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Insolvency PUT request - MethodNotAllowed status code 405")
    void callInsolvencyPutRequestMethodNotAllowed() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", URL)))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Insolvency PUT request - InternalServerError status code 500")
    void callInsolvencyPutRequestInternalServerError() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insolvency PUT request - ServiceUnavailable status code 503")
    void callInsolvencyPutRequestServiceUnavailable() throws Exception {
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(insolvencyService).processInsolvency(anyString(), anyString(),
                        isA(InternalCompanyInsolvency.class));

        mockMvc.perform(put(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key")
                        .content(gson.toJson(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Insolvency DELETE request")
    void callInsolvencyDeleteRequest() throws Exception {
        doNothing().when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insolvency DELETE request - DocumentGoneException status code 410 gone")
    void callInsolvencyDeleteRequestDocumentGone() throws Exception {
        doThrow(new DocumentNotFoundException("Document not found"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isGone());
    }

    @Test
    @DisplayName("Insolvency DELETE request - BadRequestException status code 400")
    void callInsolvencyDeleteRequestBadRequest() throws Exception {
        doThrow(new BadRequestException("Bad request - data in wrong format"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Insolvency DELETE request - MethodNotAllowed status code 405")
    void callInsolvencyDeleteRequestMethodNotAllowed() throws Exception {
        doThrow(new MethodNotAllowedException(String.format("Method Not Allowed - unsuccessful call to %s endpoint", URL)))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Insolvency DELETE request - InternalServerError status code 500")
    void callInsolvencyDeleteRequestInternalServerError() throws Exception {

        doThrow(new InternalServerErrorException("Internal Server Error - unexpected error"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insolvency DELETE request - ServiceUnavailable status code 503")
    void callInsolvencyDeleteRequestServiceUnavailable() throws Exception {

        doThrow(new ServiceUnavailableException("Service Unavailable - connection issues"))
                .when(insolvencyService).deleteInsolvency(anyString(), anyString());

        mockMvc.perform(delete(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Insolvency GET request")
    void callInsolvencyGetRequest() throws Exception {
        CompanyInsolvency companyInsolvency = new CompanyInsolvency();
        doReturn(companyInsolvency)
                .when(insolvencyService).retrieveCompanyInsolvency(anyString());

        mockMvc.perform(get(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Insolvency GET request - DocumentNotFoundException status code 404 not found")
    void callInsolvencyGetRequestDocumentnotFound() throws Exception {
        doThrow(new DocumentNotFoundException("Document not found"))
                .when(insolvencyService).retrieveCompanyInsolvency(anyString());

        mockMvc.perform(get(URL)
                        .contentType(APPLICATION_JSON)
                        .header("x-request-id", "5342342")
                        .header("ERIC-Identity" , "SOME_IDENTITY")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isNotFound());
    }
}
