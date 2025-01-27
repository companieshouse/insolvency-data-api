package uk.gov.companieshouse.insolvency.data.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SerDesException extends RuntimeException {

    public SerDesException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
