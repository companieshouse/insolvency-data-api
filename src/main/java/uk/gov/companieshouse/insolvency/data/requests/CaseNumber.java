package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CaseNumber {
    @JsonProperty("case_number")
    public int caseNumber;

    @JsonProperty("mortgage_id")
    public long mortageId;

    @JsonProperty("case_type_id")
    public int caseTypeId;

    @JsonProperty("case_type")
    public String caseType;

    @JsonProperty("appointments")
    public ArrayList<Appointment> appointments;

    @JsonProperty("wind_up_date")
    public String windUpDate;

    @JsonProperty("sworn_date")
    public String swornDate;
}
