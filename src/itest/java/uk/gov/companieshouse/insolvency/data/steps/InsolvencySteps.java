package uk.gov.companieshouse.insolvency.data.steps;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

public class InsolvencySteps {

    private String companyNumber;

    private ResponseEntity<Void> response;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InsolvencyRepository insolvencyRepository;

    @Before
    public void dbCleanUp(){
        insolvencyRepository.deleteAll();
    }

    @When("I send PUT request with payload {string}")
    public void i_send_put_request_with_payload(String string) throws IOException {
        File file = new ClassPathResource("/json/input/" + string + ".json").getFile();
        InternalCompanyInsolvency companyInsolvency = objectMapper.readValue(file, InternalCompanyInsolvency.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity request = new HttpEntity(companyInsolvency, headers);
        String uri = "/company/{company_number}/insolvency";
        String companyNumber = "CH5324324";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber);

        this.companyNumber = companyNumber;
        this.response = response;
    }
    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        Assertions.assertThat(this.response.getStatusCode()).isSameAs(HttpStatus.valueOf(statusCode));
    }

    @Then("the expected result should match {string}")
    public void the_expected_result_should_match(String string) throws IOException {
        File file = new ClassPathResource("/json/output/" + string + ".json").getFile();

        List<InsolvencyDocument> insolvencyDocuments = insolvencyRepository.findAll();

        Assertions.assertThat(insolvencyDocuments).hasSize(1);

        InsolvencyDocument actual = insolvencyRepository.findByCompanyNumber(this.companyNumber);

        InsolvencyDocument expected = objectMapper.readValue(file, InsolvencyDocument.class);

        verifyDate(actual, expected);
    }

    private void verifyDate(InsolvencyDocument actual, InsolvencyDocument expected) {

        Assertions.assertThat(actual.getCompanyInsolvency()).isEqualTo(expected.getCompanyInsolvency());
        Assertions.assertThat(actual.getCompanyNumber()).isEqualTo(expected.getCompanyNumber());
        Assertions.assertThat(actual.getUpdated().getType()).isEqualTo(expected.getUpdated().getType());
        Assertions.assertThat(actual.getUpdated().getBy()).isEqualTo(expected.getUpdated().getBy());
    }
}
