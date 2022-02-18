package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.insolvency.data.helpers.FixtureHelper;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;

import java.io.*;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class InsolvencyControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private InsolvencyService insolvencyService;
    @InjectMocks
    InsolvencyController insolvencyController;

    @Autowired
    private FixtureHelper fixtureHelper;

    Gson g = new Gson();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(insolvencyController)
                .build();
    }

    @Test
    @DisplayName("Successfully returns health status")
    public void returnHealthStatusSuccessfully() throws Exception {

        String jsonFile = fixtureHelper.readJsonFile("Insolvency");
        InsolvencyRequest insolvencyRequest = g.fromJson(jsonFile, InsolvencyRequest.class);
        doNothing().when(insolvencyService).saveInsolvency(isA(InsolvencyRequest.class));
        mockMvc.perform(put("/company/123/insolvency").contentType(APPLICATION_JSON)
                .content(jsonFile)).andExpect(status().isOk());


        verify(insolvencyService).saveInsolvency(insolvencyRequest);
    }


}
