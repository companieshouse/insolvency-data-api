package uk.gov.companieshouse.insolvency.data.exceptions;

public class DocumentGoneException extends RuntimeException {

    public DocumentGoneException(String message) {
        super(message);
    }
}
