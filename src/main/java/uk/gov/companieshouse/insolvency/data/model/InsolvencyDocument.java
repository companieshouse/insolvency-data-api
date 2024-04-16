package uk.gov.companieshouse.insolvency.data.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;

@Document(collection = "#{@environment.getProperty('mongodb.insolvency.collection.name')}")
public class InsolvencyDocument {

    @Id
    private String id;

    @Field("data")
    private CompanyInsolvency companyInsolvency;

    @Field("delta_at")
    private OffsetDateTime deltaAt;

    @Field("updated_at")
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;

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
                              OffsetDateTime deltaAt,
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

    public OffsetDateTime getDeltaAt() {
        return deltaAt;
    }

    public void setDeltaAt(OffsetDateTime deltaAt) {
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
}
