package uk.gov.companieshouse.insolvency.data.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Document(collection = "company_insolvency")
public class InsolvencyDocument {

    @Id
    private String id;

    @Field("data")
    private CompanyInsolvency companyInsolvency;

    private Updated updated;

    // Temporary removed as was creating issues during update
    //    @Version
    //    private Long version;

    /**
     * Default constructor.
     */
    public InsolvencyDocument() {
    }

    /**
     * Instantiate company insolvency document.
     * @param id the company number
     * @param companyInsolvency company insolvency data
     * @param updated company insolvency updated details
     */
    public InsolvencyDocument(String id, CompanyInsolvency companyInsolvency,
                              Updated updated) {
        this.id = id;
        this.companyInsolvency = companyInsolvency;
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public CompanyInsolvency getCompanyInsolvency() {
        return companyInsolvency;
    }

    public Updated getUpdated() {
        return updated;
    }
}
