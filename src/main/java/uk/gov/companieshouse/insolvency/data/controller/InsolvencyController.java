package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;
import uk.gov.companieshouse.logging.Logger;

@RestController
public class InsolvencyController {

    private final Logger logger;
    private final InsolvencyService insolvencyService;

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
    public ResponseEntity<Void> insolvency(
            @PathVariable("company_number") String companyNumber,
            @RequestBody InternalCompanyInsolvency requestBody
    ) throws JsonProcessingException {

        insolvencyService.saveInsolvency(companyNumber, requestBody);

        logger.info(String.format("Company insolvency updated successfully for company number %s",
                companyNumber));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
