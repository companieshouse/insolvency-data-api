package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import uk.gov.companieshouse.api.delta.Insolvency;

public class InsolvencyRequest {
    @JsonProperty("insolvency")
    public ArrayList<Insolvency> insolvency;
}

