package uk.gov.companieshouse.insolvency.data.steps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.config.CucumberContext;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.insolvency.data.config.AbstractMongoConfig.mongoDBContainer;

public class InsolvencySteps {

    private String companyNumber;
    private String contextId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InsolvencyRepository insolvencyRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public InsolvencyApiService insolvencyApiService;

    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        insolvencyRepository.deleteAll();
    }

    @Given("the insolvency information exists for {string}")
    public void the_insolvency_information_exists_for(String companyNumber) throws IOException {
        File file = new ClassPathResource("/json/input/case_type_compulsory_liquidation.json").getFile();
        InternalCompanyInsolvency companyInsolvency = objectMapper.readValue(file, InternalCompanyInsolvency.class);

        InternalData internalData = companyInsolvency.getInternalData();
        InsolvencyDocument insolvencyDocument = new InsolvencyDocument(companyNumber,
                companyInsolvency.getExternalData(),
                internalData.getDeltaAt(),
                LocalDateTime.now(),
                internalData.getUpdatedBy());

        mongoTemplate.save(insolvencyDocument);
    }

    @Given("the insolvency database is down")
    public void the_insolvency_db_is_down() {
        mongoDBContainer.stop();
    }

    @When("I send GET request with company number {string}")
    public void i_send_get_request_with_company_number(String companyNumber) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");

        HttpEntity<?> request = new HttpEntity<>(headers);

        String uri = "/company/{company_number}/insolvency";
        ResponseEntity<CompanyInsolvency> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                CompanyInsolvency.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }

    @When("I send GET request with company number {string} without eric headers")
    public void i_send_get_request_with_company_number_without_eric_header(String companyNumber) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);

        String uri = "/company/{company_number}/insolvency";
        ResponseEntity<CompanyInsolvency> response = restTemplate.exchange(uri, HttpMethod.GET, request,
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

        this.contextId = "5234234234";
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");

        HttpEntity<?> request = new HttpEntity<>(companyInsolvency, headers);
        String uri = "/company/{company_number}/insolvency";
        String companyNumber = "CH5324324";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber);

        this.companyNumber = companyNumber;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send PUT request with payload {string} file without eric headers")
    public void i_send_put_request_with_payload_without_eric_header(String string) throws IOException {
        File file = new ClassPathResource("/json/input/" + string + ".json").getFile();
        InternalCompanyInsolvency companyInsolvency = objectMapper.readValue(file, InternalCompanyInsolvency.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        headers.set("x-request-id", this.contextId);

        HttpEntity<?> request = new HttpEntity<>(companyInsolvency, headers);
        String uri = "/company/{company_number}/insolvency";
        String companyNumber = "CH5324324";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber);

        this.companyNumber = companyNumber;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("I send PUT request with raw payload {string} file")
    public void i_send_put_request_with_raw_payload(String string) throws IOException {
        File file = new ClassPathResource("/json/input/" + string + ".json").getFile();
        String raw_payload = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");

        HttpEntity<?> request = new HttpEntity<>(raw_payload, headers);
        String uri = "/company/{company_number}/insolvency";
        String companyNumber = "CH5324324";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, companyNumber);

        this.companyNumber = companyNumber;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() throws IOException {
        doThrow(ServiceUnavailableException.class)
                .when(insolvencyApiService).invokeChsKafkaApi(anyString(), any(), any());
    }

    @When("I send DELETE request with company number {string}")
    public void i_send_delete_request_with_company_number(String companyNumber) throws IOException {
        String uri = "/company/{company_number}/insolvency";

        HttpHeaders headers = new HttpHeaders();
        this.contextId = "5234234234";
        headers.set("x-request-id", "5234234234");
        headers.set("ERIC-Identity" , "SOME_IDENTITY");
        headers.set("ERIC-Identity-Type", "key");
        var request = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        this.companyNumber = companyNumber;
    }

    @When("I send DELETE request with company number {string} without setting eric headers")
    public void i_send_delete_request_with_company_number_no_eric_headers(String companyNumber) throws IOException {
        String uri = "/company/{company_number}/insolvency";

        HttpHeaders headers = new HttpHeaders();
        this.contextId = "5234234234";
        headers.set("x-request-id", "5234234234");
        var request = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        this.companyNumber = companyNumber;
    }

    @When("I send DELETE request without x-request-id key in the header")
    public void i_send_delete_request_without_x_request_id_key_in_the_header() {
        String uri = "/company/CH1234567/insolvency";

        HttpHeaders headers = new HttpHeaders();
        this.contextId = "5234234234";
        var request = new HttpEntity<>(null, headers);

        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.DELETE, request, Void.class, companyNumber);

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

        Optional<InsolvencyDocument> actual = insolvencyRepository.findById(this.companyNumber);

        assertThat(actual).isPresent();

        InsolvencyDocument expected = objectMapper.readValue(file, InsolvencyDocument.class);

        InsolvencyDocument actualDocument = actual.get();

        // Verify that the time inserted is after the input
        assertThat(actualDocument.getUpdatedAt()).isAfter(expected.getUpdatedAt());

        // Matching both updatedAt since it will never match the output (Uses now time)
        LocalDateTime replacedLocalDateTime = LocalDateTime.now();
        expected.setUpdatedAt(replacedLocalDateTime);
        actualDocument.setUpdatedAt(replacedLocalDateTime);
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

    @Then("the CHS Kafka API is invoked successfully with event {string}")
    public void chs_kafka_api_invoked(String event) throws IOException {
        Optional<EventType> eventType = EventType.getEventType(event);
        assertThat(eventType).isPresent();
        verify(insolvencyApiService).invokeChsKafkaApi(anyString(), any(), eq(eventType.get()));
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() throws IOException {
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(),
                any(InsolvencyDocument.class), any());
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_database() {
        List<InsolvencyDocument> insolvencyDocuments = insolvencyRepository.findAll();
        Assertions.assertThat(insolvencyDocuments).isEmpty();
    }

    private void verifyPutData(InsolvencyDocument actual, InsolvencyDocument expected) {
        CompanyInsolvency actualCompanyInsolvency = actual.getCompanyInsolvency();
        CompanyInsolvency expectedCompanyInsolvency = expected.getCompanyInsolvency();

        assertThat(actualCompanyInsolvency.getCases()).isEqualTo(expectedCompanyInsolvency.getCases());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getUpdatedAt()).isEqualTo(expected.getUpdatedAt());
        assertThat(actual.getUpdatedBy()).isEqualTo(expected.getUpdatedBy());
        assertThat(actual.getDeltaAt()).isEqualTo(expected.getDeltaAt());
    }

}
