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
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

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
        String contextId = "436534543";
        String companyNumber = "CH363453";

        underTest.processInsolvency(contextId, companyNumber, companyInsolvency);

        verify(repository, Mockito.times(1)).save(Mockito.any());
        verify(insolvencyApiService, times(1)).invokeChsKafkaApi(eq(contextId), any(),
                eq(EventType.CHANGED));
    }

    @Test
    void when_connection_issue_in_db_then_throw_service_unavailable_exception() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                underTest.processInsolvency("436534543", "CH363453", companyInsolvency));
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(),
                any());

    }

    @Test
    void when_insolvency_number_is_given_then_return_company_insolvency_information() {
        String companyNumber = "234234";

        InsolvencyDocument document = new InsolvencyDocument(companyNumber, new CompanyInsolvency(), LocalDateTime.now(), LocalDateTime.now(), "123");
        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.of(document));

        CompanyInsolvency companyInsolvency = underTest.retrieveCompanyInsolvency(companyNumber);

        Assertions.assertThat(companyInsolvency).isNotNull();
        verify(repository, Mockito.times(1)).findById(Mockito.any());

    }

    @Test
    void when_invalid_put_request_then_throw_bad_request_exception() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new IllegalArgumentException())
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        Assert.assertThrows(BadRequestException.class, () ->
                underTest.processInsolvency("436534543", "CH363453", companyInsolvency));
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void when_invalid_insolvency_number_is_given_then_throw_exception() {
        Assert.assertThrows(RuntimeException.class, () -> underTest.retrieveCompanyInsolvency
                ("CH4000056"));

        verify(repository, Mockito.times(1)).findById(Mockito.any());
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void when_company_number_doesnt_exist_then_throws_IllegalArgumentExceptionException_error() {
        String companyNumber = "CH363453";
        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.empty());

        Assert.assertThrows(IllegalArgumentException.class, () ->
                underTest.deleteInsolvency(companyNumber, companyNumber));

        verify(repository, Mockito.times(0)).deleteById(Mockito.any());
        verify(repository, Mockito.times(1)).findById(Mockito.eq(companyNumber));
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void when_company_number_exist_then_finishes_successfully() {
        String companyNumber = "CH363453";
        String contextId = "1234";
        InsolvencyDocument document = new InsolvencyDocument(companyNumber, new CompanyInsolvency(), LocalDateTime.now(), LocalDateTime.now(), "123");
        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.of(document));

        underTest.deleteInsolvency(contextId, companyNumber);
        verify(logger, Mockito.times(1)).info(
                "Company insolvency delete called for company number " + companyNumber
        );
        verify(repository, Mockito.times(1)).deleteById(Mockito.any());
        verify(repository, Mockito.times(1)).findById(Mockito.eq(companyNumber));
        verify(insolvencyApiService, times(1)).invokeChsKafkaApi(eq(contextId), eq(document), eq(EventType.DELETED));
    }

    @Test
    void when_connection_issue_in_db_on_delete_then_throw_service_unavailable_exception() {
        String companyNumber = "CH363453";

        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.of(new InsolvencyDocument()));
        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .deleteById(companyNumber);

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                underTest.deleteInsolvency("436534543", companyNumber));
    }

    @Test
    void when_connection_issue_in_db_on_find_in_delete_then_throw_service_unavailable_exception() {
        String companyNumber = "CH363453";

        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .findById(companyNumber);

        Assert.assertThrows(ServiceUnavailableException.class, () ->
                underTest.deleteInsolvency("436534543", companyNumber));
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
