package uk.gov.companieshouse.insolvency.data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Document(collection = "company_insolvency")
public class InsolvencyDocument {

    @Id
    private String id;

    private final String companyNumber;

    @Field(name = "data")
    private final CompanyInsolvency companyInsolvency;

    private final Updated updated;

    /**
     * Instantiate company insolvency document.
     * @param companyNumber the company number
     * @param companyInsolvency company insolvency data
     * @param updated company insolvency updated details
     */
    public InsolvencyDocument(String companyNumber, CompanyInsolvency companyInsolvency,
                              Updated updated) {
        this.companyNumber = companyNumber;
        this.companyInsolvency = companyInsolvency;
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public CompanyInsolvency getCompanyInsolvency() {
        return companyInsolvency;
    }

    public Updated getUpdated() {
        return updated;
    }
}
