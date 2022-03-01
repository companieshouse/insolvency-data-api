package uk.gov.companieshouse.insolvency.data.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.delta.Insolvency;
import uk.gov.companieshouse.insolvency.data.requests.InsolvencyRequest;

@Service
public class InsolvencyService {

    public InsolvencyService() {
    }

    /**
     * Save insolvency service layer method.
     *
     * @param  companyNumber  companyNumber
     * @param  insolvency  the insolvency request data
     */
    public void saveInsolvency(
            String companyNumber,
            InsolvencyRequest insolvency) throws JsonProcessingException {
        // TODO Save to database
    }
}
