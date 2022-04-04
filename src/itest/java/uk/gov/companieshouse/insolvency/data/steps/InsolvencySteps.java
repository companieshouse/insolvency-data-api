package uk.gov.companieshouse.insolvency.data.steps;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.config.CucumberContext;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.model.Updated;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

import static org.assertj.core.api.Assertions.assertThat;
public class InsolvencySteps {

    private String companyNumber;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InsolvencyRepository insolvencyRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void dbCleanUp(){
        insolvencyRepository.deleteAll();
    }

    @Given("the insolvency information exists for {string}")
    public void the_insolvency_information_exists_for(String companyNumber) throws IOException {
        File file = new ClassPathResource("/json/input/case_type_compulsory_liquidation.json").getFile();
        InternalCompanyInsolvency companyInsolvency = objectMapper.readValue(file, InternalCompanyInsolvency.class);

        InternalData internalData = companyInsolvency.getInternalData();
        Updated updated = new Updated(internalData.getDeltaAt().toString(),
                internalData.getUpdatedBy(), "company-insolvency");
        InsolvencyDocument insolvencyDocument = new InsolvencyDocument(companyNumber,
                companyInsolvency.getExternalData(), updated);

        mongoTemplate.save(insolvencyDocument);
    }

    @When("I send GET request with company number {string}")
    public void i_send_get_request_with_company_number(String companyNumber) throws IOException {
        String uri = "/company/{company_number}/insolvency";
        ResponseEntity<CompanyInsolvency> response = restTemplate.exchange(uri, HttpMethod.GET, null,
                CompanyInsolvency.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("I send PUT request with payload {string} file")
    public void i_send_put_request_with_payload(String string) throws IOException {
        File file = new ClassPathResource("/json/input/" + string + ".json").getFile();
        InternalCompanyInsolvency companyInsolvency = objectMapper.readValue(file, InternalCompanyInsolvency.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-request-id", "5234234234");

        HttpEntity request = new HttpEntity(companyInsolvency, headers);
        String uri = "/company/{company_number}/insolvency";
        String companyNumber = "CH5324324";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber);

        this.companyNumber = companyNumber;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @Then("the expected result should match {string} file")
    public void the_expected_result_should_match(String string) throws IOException {
        File file = new ClassPathResource("/json/output/" + string + ".json").getFile();

        List<InsolvencyDocument> insolvencyDocuments = insolvencyRepository.findAll();

        Assertions.assertThat(insolvencyDocuments).hasSize(1);

        Optional<InsolvencyDocument> actual = insolvencyRepository.findByCompanyNumber(this.companyNumber);

        assertThat(actual.isPresent()).isTrue();

        InsolvencyDocument expected = objectMapper.readValue(file, InsolvencyDocument.class);

        verifyPutData(actual.get(), expected);
    }

    @Then("the Get call response body should match {string} file")
    public void the_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/"+dataFile+".json").getFile();

        InsolvencyDocument expectedDocument = objectMapper.readValue(file, InsolvencyDocument.class);

        CompanyInsolvency expected = expectedDocument.getCompanyInsolvency();
        CompanyInsolvency actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getStatus()).isEqualTo(actual.getStatus());
        assertThat(expected.getCases()).isEqualTo(actual.getCases());
    }

    private void verifyPutData(InsolvencyDocument actual, InsolvencyDocument expected) {
        CompanyInsolvency actualCompanyInsolvency = actual.getCompanyInsolvency();
        CompanyInsolvency expectedCompanyInsolvency = expected.getCompanyInsolvency();

        assertThat(actualCompanyInsolvency.getCases()).isEqualTo(expectedCompanyInsolvency.getCases());
        assertThat(actual.getCompanyNumber()).isEqualTo(expected.getCompanyNumber());
        assertThat(actual.getUpdated().getType()).isEqualTo(expected.getUpdated().getType());
        assertThat(actual.getUpdated().getBy()).isEqualTo(expected.getUpdated().getBy());
    }

}
