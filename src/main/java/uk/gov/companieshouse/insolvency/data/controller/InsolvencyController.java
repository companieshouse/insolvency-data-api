package uk.gov.companieshouse.insolvency.data.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;
import uk.gov.companieshouse.logging.Logger;


@RestController
public class InsolvencyController {

    private final Logger logger;
    private final InsolvencyService insolvencyService;
    private final InsolvencyApiService insolvencyApiService;

    /**
     * Endpoint to handle company insolvency information.
     * @param logger to log statements
     * @param insolvencyService service to store the collection
     * @param insolvencyApiService service to call chs-kafka api
     */
    public InsolvencyController(Logger logger, InsolvencyService insolvencyService,
                                InsolvencyApiService insolvencyApiService) {
        this.logger = logger;
        this.insolvencyService = insolvencyService;
        this.insolvencyApiService = insolvencyApiService;
    }

    /**
     * PUT request for insolvency.
     *
     * @param  companyNumber  the company number for insolvency
     * @param  requestBody  the request body containing insolvency data
     * @return  no response
     */
    @PutMapping("/company/{company_number}/insolvency")
    @Transactional
    public ResponseEntity<Void> insolvency(
            @PathVariable("company_number") String companyNumber,
            @RequestBody InternalCompanyInsolvency requestBody
    ) throws JsonProcessingException {

        insolvencyService.saveInsolvency(companyNumber, requestBody);

        logger.info(String.format(
                "Company insolvency collection updated successfully for company number %s",
                companyNumber));

        insolvencyApiService.invokeChsKafkaApi(companyNumber);

        logger.info(String.format("ChsKafka api invoked successfully for company number %s",
                companyNumber));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
