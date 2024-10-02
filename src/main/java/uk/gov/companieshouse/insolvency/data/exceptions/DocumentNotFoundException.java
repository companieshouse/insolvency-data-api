package uk.gov.companieshouse.insolvency.data.exceptions;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
