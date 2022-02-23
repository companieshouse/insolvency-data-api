package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;

import java.nio.charset.StandardCharsets;

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

    @Value("classpath:data/InsolvencyRequest.json")
    private Resource resourceFile;

    private ObjectMapper mapper = new ObjectMapper();

    private Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(insolvencyController)
                .build();
    }

    @Test
    @DisplayName("Insolvency PUT request")
    public void returnHealthStatusSuccessfully() throws Exception {
        String jsonFile = FileUtils.readFileToString(resourceFile.getFile(), StandardCharsets.UTF_8);
        InsolvencyRequest insolvencyRequest = mapper.readValue(jsonFile, InsolvencyRequest.class);
        doNothing().when(insolvencyService).saveInsolvency(isA(InsolvencyRequest.class));
        String url = String.format("/company/%s/insolvency", "02588581");
        mockMvc.perform(put(url).contentType(APPLICATION_JSON)
                .content(jsonFile)).andExpect(status().isOk());

        verify(insolvencyService).saveInsolvency(argThat((insolvencyArgument) -> {
            assert(gson.toJson(insolvencyArgument).equals(gson.toJson(insolvencyRequest)));
            return true;
        }));
    }


}
