package uk.gov.companieshouse.insolvency.data.controller;

import static uk.gov.companieshouse.insolvency.data.InsolvencyDataApiApplication.NAMESPACE;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.logging.DataMapHolder;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;


@RestController
public class InsolvencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final InsolvencyService insolvencyService;

    /**
     * Endpoint to handle company insolvency information.
     *
     * @param insolvencyService service to store the collection
     */
    public InsolvencyController(InsolvencyService insolvencyService) {
        this.insolvencyService = insolvencyService;
    }

    /**
     * PUT request for insolvency.
     *
     * @param companyNumber the company number for insolvency
     * @param requestBody   the request body containing insolvency data
     * @return no response
     */
    @PutMapping("/company/{company_number}/insolvency")
    public ResponseEntity<Void> insolvency(@RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @Valid @RequestBody InternalCompanyInsolvency requestBody
    ) {
        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("Payload successfully received for PUT request", DataMapHolder.getLogMap());
        LOGGER.info("TEST");
        insolvencyService.processInsolvency(companyNumber, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieve company insolvency information for a company number.
     *
     * @param companyNumber the company number for insolvency
     * @return {@link CompanyInsolvency} return company insolvency information
     */
    @GetMapping("/company/{company_number}/insolvency")
    public ResponseEntity<CompanyInsolvency> insolvency(
            @PathVariable("company_number") String companyNumber) {
        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("Retrieving company insolvency information", DataMapHolder.getLogMap());
        CompanyInsolvency companyInsolvency = insolvencyService.retrieveCompanyInsolvency(
                companyNumber);

        return ResponseEntity.status(HttpStatus.OK).body(companyInsolvency);
    }

    /**
     * Retrieve company insolvency information for a company number.
     *
     * @param companyNumber the company number for insolvency
     * @return {@link CompanyInsolvency} return company insolvency information
     */
    @DeleteMapping("/company/{company_number}/insolvency")
    public ResponseEntity<Void> insolvency(
            @PathVariable("company_number") String companyNumber,
            @RequestHeader("X-DELTA-AT") String deltaAt) {
        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("DELETE request successfully received", DataMapHolder.getLogMap());

        insolvencyService.deleteInsolvency(companyNumber, deltaAt);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
