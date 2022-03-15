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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyServiceImpl;
import uk.gov.companieshouse.logging.Logger;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class InsolvencyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Logger logger;

    @Mock
    private InsolvencyServiceImpl insolvencyService;

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
        InternalCompanyInsolvency request = new InternalCompanyInsolvency();
        request.setInternalData(new InternalData());
        request.setExternalData(new CompanyInsolvency());

        String url = String.format("/company/%s/insolvency", "02588581");
        mockMvc.perform(put(url).contentType(APPLICATION_JSON)
                .content(gson.toJson(request))).andExpect(status().isOk());
    }

}
