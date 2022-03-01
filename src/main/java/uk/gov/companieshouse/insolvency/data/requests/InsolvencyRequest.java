package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;

public class InsolvencyRequest {
    @JsonProperty("insolvency")
    public InternalCompanyInsolvency insolvency;
}

