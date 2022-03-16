package uk.gov.companieshouse.insolvency.data.model;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;

public class InternalCompanyInsolvency2 {
    @JsonProperty("external_data")
    private CompanyInsolvency externalData;
    @JsonProperty("internal_data")
    private InternalData internalData;

    @JsonProperty("appointed_on")
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate appointedOn;

    public InternalCompanyInsolvency2() {
    }

    public InternalCompanyInsolvency2 externalData(CompanyInsolvency externalData) {
        this.externalData = externalData;
        return this;
    }

    @Valid
    public CompanyInsolvency getExternalData() {
        return this.externalData;
    }

    public void setExternalData(CompanyInsolvency externalData) {
        this.externalData = externalData;
    }

    public InternalCompanyInsolvency2 internalData(InternalData internalData) {
        this.internalData = internalData;
        return this;
    }

    @Valid
    public InternalData getInternalData() {
        return this.internalData;
    }

    public void setInternalData(InternalData internalData) {
        this.internalData = internalData;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            InternalCompanyInsolvency2 internalCompanyInsolvency = (InternalCompanyInsolvency2)o;
            return Objects.equals(this.externalData, internalCompanyInsolvency.externalData) && Objects.equals(this.internalData, internalCompanyInsolvency.internalData);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.externalData, this.internalData});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InternalCompanyInsolvency {\n");
        sb.append("    externalData: ").append(this.toIndentedString(this.externalData)).append("\n");
        sb.append("    internalData: ").append(this.toIndentedString(this.internalData)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
