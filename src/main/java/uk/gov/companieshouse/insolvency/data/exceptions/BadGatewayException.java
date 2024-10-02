package uk.gov.companieshouse.insolvency.data.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }
}
