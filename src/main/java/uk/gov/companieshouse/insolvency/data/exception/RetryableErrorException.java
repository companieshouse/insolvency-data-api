package uk.gov.companieshouse.insolvency.data.exception;

public class RetryableErrorException extends RuntimeException {
    public RetryableErrorException(String message) {
        super(message);
    }
}

