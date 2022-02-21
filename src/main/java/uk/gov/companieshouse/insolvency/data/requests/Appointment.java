package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Appointment {
    @JsonProperty("forename")
    public String forename;

    @JsonProperty("middle_name")
    public String middleName;

    @JsonProperty("surname")
    public String surname;

    @JsonProperty("appt_type")
    public int apptType;

    @JsonProperty("appt_date")
    public String apptDate;

    @JsonProperty("ceased_to_act_appt")
    public String ceasedToActAppt;

    @JsonProperty("practitioner_address")
    public PractitionerAddress practitionerAddress;
}