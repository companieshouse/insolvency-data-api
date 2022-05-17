package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;
import uk.gov.companieshouse.logging.Logger;


@RestController
public class InsolvencyController {

    private final Logger logger;
    private final InsolvencyService insolvencyService;

    /**
     * Endpoint to handle company insolvency information.
     * @param logger to log statements
     * @param insolvencyService service to store the collection
     */
    public InsolvencyController(Logger logger, InsolvencyService insolvencyService) {
        this.logger = logger;
        this.insolvencyService = insolvencyService;
    }

    /**
     * PUT request for insolvency.
     *
     * @param  companyNumber  the company number for insolvency
     * @param  requestBody  the request body containing insolvency data
     * @return  no response
     */
    @PutMapping("/company/{company_number}/insolvency")
    public ResponseEntity<Void> insolvency(@RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber,
            @RequestBody InternalCompanyInsolvency requestBody
    ) throws JsonProcessingException {
        logger.info(String.format(
                "Payload Successfully received on PUT with context id %s and company number %s",
                contextId,
                companyNumber));

        insolvencyService.processInsolvency(contextId, companyNumber, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieve company insolvency information for a company number.
     *
     * @param  companyNumber  the company number for insolvency
     * @return  {@link CompanyInsolvency} return company insolvency information
     */
    @GetMapping("/company/{company_number}/insolvency")
    public ResponseEntity<CompanyInsolvency> insolvency(
            @PathVariable("company_number") String companyNumber) {
        logger.info(String.format(
                "Retrieving company insolvency information for company number %s",
                companyNumber));

        CompanyInsolvency companyInsolvency = insolvencyService.retrieveCompanyInsolvency(
                companyNumber);

        return ResponseEntity.status(HttpStatus.OK).body(companyInsolvency);
    }

    /**
     * Retrieve company insolvency information for a company number.
     *
     * @param  companyNumber  the company number for insolvency
     * @return  {@link CompanyInsolvency} return company insolvency information
     */
    @DeleteMapping("/company/{company_number}/insolvency")
    public ResponseEntity<Void> insolvency(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("company_number") String companyNumber) {
        logger.info(String.format(
                "Payload Successfully received on DELETE with context id %s and company number %s",
                contextId,
                companyNumber));
        insolvencyService.deleteInsolvency(contextId, companyNumber);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
