package uk.gov.companieshouse.insolvency.data.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;
import uk.gov.companieshouse.insolvency.data.service.InsolvencyService;

@RestController
public class InsolvencyController {

    @Autowired
    private InsolvencyService insolvencyService;

    /**
     * Java doc here TODO
     */
    @PutMapping("/company/{company_number}/insolvency")
    public ResponseEntity<Void> insolvency(
            @PathVariable("company_number") int companyNumber,
            @RequestBody InsolvencyRequest requestBody
    ) {
        insolvencyService.saveInsolvency(requestBody);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
