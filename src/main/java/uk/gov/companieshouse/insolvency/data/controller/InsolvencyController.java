package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
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
        RequestContext.setId(contextId);
        logger.info(String.format(
                "Processing company insolvency information for company number %s",
                companyNumber));

        insolvencyService.processInsolvency(companyNumber, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
