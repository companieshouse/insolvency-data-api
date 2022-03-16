package uk.gov.companieshouse.insolvency.data.controller;

import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.insolvency.ModelCase;
import uk.gov.companieshouse.api.insolvency.Practitioners;
import uk.gov.companieshouse.insolvency.data.model.InternalCompanyInsolvency2;
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
            @RequestBody InternalCompanyInsolvency2 requestBody
    ) throws JsonProcessingException {
        ModelCase newCase = new ModelCase();
        Practitioners prac = new Practitioners();
        prac.setName("test");
        prac.setAppointedOn(LocalDate.now());
        newCase.getPractitioners().add(prac);
        requestBody.getExternalData().getCases().add(newCase);

        insolvencyService.saveInsolvency(companyNumber, requestBody);

        logger.info(String.format("Company insolvency updated successfully for company number %s",
                companyNumber));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
