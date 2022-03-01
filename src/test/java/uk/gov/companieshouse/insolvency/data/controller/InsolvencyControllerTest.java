package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class InsolvencyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InsolvencyService insolvencyService;

    @InjectMocks
    private InsolvencyController insolvencyController;

    private ObjectMapper mapper = new ObjectMapper();

    private Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(insolvencyController)
                .build();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @DisplayName("Insolvency PUT request")
    public void callInsolvencyPutRequest() throws Exception {

        InsolvencyRequest request = new InsolvencyRequest();
        doNothing().when(insolvencyService).saveInsolvency(eq("02588581"), isA(InsolvencyRequest.class));
        String url = String.format("/company/%s/insolvency", "02588581");
        mockMvc.perform(put(url).contentType(APPLICATION_JSON)
                .content(gson.toJson(request))).andExpect(status().isOk());
    }


}
