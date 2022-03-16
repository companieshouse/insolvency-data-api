package uk.gov.companieshouse.insolvency.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Document(collection = "company_insolvency")
public class InsolvencyDocument {

    @Id
    private String id;

    @Field("company_number")
    private final String companyNumber;

    @Field("data")
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
