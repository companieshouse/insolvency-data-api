package uk.gov.companieshouse.insolvency.data.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Document(collection = "company_insolvency")
public class InsolvencyDocument {

    @Id
    private String id;

    @Field("data")
    private CompanyInsolvency companyInsolvency;

    @Field("delta_at")
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private LocalDateTime deltaAt;

    @Field("updated_at")
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;

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
     */
    public InsolvencyDocument(String id,
                              CompanyInsolvency companyInsolvency,
                              LocalDateTime deltaAt,
                              LocalDateTime updatedAt,
                              String updatedBy) {
        this.id = id;
        this.companyInsolvency = companyInsolvency;
        this.deltaAt = deltaAt;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getId() {
        return id;
    }

    public CompanyInsolvency getCompanyInsolvency() {
        return companyInsolvency;
    }

    public LocalDateTime getDeltaAt() {
        return deltaAt;
    }

    public void setDeltaAt(LocalDateTime deltaAt) {
        this.deltaAt = deltaAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
