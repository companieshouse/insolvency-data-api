package uk.gov.companieshouse.insolvency.data.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "company_insolvency")
public class Insolvency {
    @JsonProperty("delta_at")
    public String deltaAt;

    @JsonProperty("company_number")
    public String companyNumber;

    @JsonProperty("case_numbers")
    public ArrayList<CaseNumber> caseNumbers;
}

