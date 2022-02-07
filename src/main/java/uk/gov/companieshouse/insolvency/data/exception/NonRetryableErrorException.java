package uk.gov.companieshouse.insolvency.data.exception;

public class NonRetryableErrorException extends RuntimeException {
    public NonRetryableErrorException(String message) {
        super(message);
    }
}

