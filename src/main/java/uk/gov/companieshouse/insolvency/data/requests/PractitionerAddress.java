package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PractitionerAddress {
    @JsonProperty("address_line_1")
    public String addressLine1;

    @JsonProperty("address_line_2")
    public String addressLine2;

    @JsonProperty("locality")
    public String locality;

    @JsonProperty("region")
    public String region;

    @JsonProperty("country")
    public String country;

    @JsonProperty("postal_code")
    public String postalCode;
}