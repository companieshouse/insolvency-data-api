package uk.gov.companieshouse.insolvency.data.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public ResponseEntity<Void> healthcheck() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
