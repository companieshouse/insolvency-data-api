package uk.gov.companieshouse.insolvency.data.exceptions;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }
}
