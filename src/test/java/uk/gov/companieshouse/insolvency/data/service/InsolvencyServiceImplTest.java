package uk.gov.companieshouse.insolvency.data.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsolvencyServiceImplTest {

    @Mock
    private InsolvencyRepository repository;

    @Mock
    private Logger logger;

    @Mock
    private InsolvencyApiService insolvencyApiService;

    @InjectMocks
    private InsolvencyServiceImpl underTest;

    @Test
    void when_insolvency_data_is_given_then_data_should_be_saved() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        underTest.processInsolvency("436534543", "CH363453", companyInsolvency);

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void when_connection_issue_in_db_then_throw_service_unavailable_exception() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                underTest.processInsolvency("436534543", "CH363453", companyInsolvency));
    }

    @Test
    void when_insolvency_number_is_given_then_return_company_insolvency_information() {
        String companyNumber = "234234";
        InsolvencyDocument document = new InsolvencyDocument(companyNumber, new CompanyInsolvency(), LocalDateTime.now(), LocalDateTime.now(), "123");
        Mockito.when(repository.findById("234234")).thenReturn(Optional.of(document));

        CompanyInsolvency companyInsolvency = underTest.retrieveCompanyInsolvency("234234");

        Assertions.assertThat(companyInsolvency).isNotNull();
        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void when_invalid_put_request_then_throw_bad_request_exception() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new IllegalArgumentException())
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        Assert.assertThrows(BadRequestException.class, () ->
                underTest.processInsolvency("436534543", "CH363453", companyInsolvency));
    }

    @Test
    void when_invalid_insolvency_number_is_given_then_throw_exception() {
        Assert.assertThrows(RuntimeException.class, () -> underTest.retrieveCompanyInsolvency
                ("CH4000056"));

        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.any());
    }


    private InternalCompanyInsolvency createInternalCompanyInsolvency() {
        InternalCompanyInsolvency companyInsolvency = new InternalCompanyInsolvency();
        InternalData internalData = new InternalData();
        internalData.setDeltaAt(OffsetDateTime.now());
        CompanyInsolvency externalData = new CompanyInsolvency();

        companyInsolvency.setExternalData(externalData);
        companyInsolvency.setInternalData(internalData);

        return companyInsolvency;
    }

}
