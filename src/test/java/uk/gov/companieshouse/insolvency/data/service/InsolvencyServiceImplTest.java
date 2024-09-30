package uk.gov.companieshouse.insolvency.data.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.BadRequestException;
import uk.gov.companieshouse.insolvency.data.exceptions.DocumentNotFoundException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

@ExtendWith(MockitoExtension.class)
class InsolvencyServiceImplTest {

    @Mock
    private InsolvencyRepository repository;
    @Mock
    private InsolvencyApiService insolvencyApiService;

    @InjectMocks
    private InsolvencyServiceImpl underTest;

    @ParameterizedTest
    @CsvSource({
            "2021-03-08T12:00:00.000Z , 2022-03-08T12:00:00.000Z",
            "2022-03-08T12:00:00.000Z , 2022-03-08T12:00:00.001Z"
    })
    void when_request_is_stale_then_data_should_not_be_saved(OffsetDateTime requestDeltaAt,
            OffsetDateTime existingDeltaAt) {
        // given
        final String companyNumber = "CN123456";
        final String contextId = "context_id";

        InternalCompanyInsolvency internalCompanyInsolvency = createInternalCompanyInsolvency();
        internalCompanyInsolvency.getInternalData().setDeltaAt(requestDeltaAt);

        InsolvencyDocument existingDocument = new InsolvencyDocument();
        existingDocument.setDeltaAt(existingDeltaAt);
        Optional<InsolvencyDocument> existingDocumentOptional = Optional.of(existingDocument);

        when(repository.findById(anyString())).thenReturn(existingDocumentOptional);

        // when
        Executable executable = () -> underTest.processInsolvency(contextId, companyNumber, internalCompanyInsolvency);

        // then
        assertThrows(BadRequestException.class, executable);
        verifyNoInteractions(insolvencyApiService);
        verify(repository, times(0)).save(any());
    }

    @Test
    void should_process_successfully_when_existing_delta_at_is_null() {
        // given
        final String companyNumber = "CN123456";
        final String contextId = "context_id";

        OffsetDateTime requestDeltaAt = OffsetDateTime.parse("2021-03-08T12:00:00.000Z");

        InternalCompanyInsolvency internalCompanyInsolvency = createInternalCompanyInsolvency();
        internalCompanyInsolvency.getInternalData().setDeltaAt(requestDeltaAt);
        InsolvencyDocument existingDocument = new InsolvencyDocument();

        when(repository.findById(anyString())).thenReturn(Optional.of(existingDocument));

        // when
        underTest.processInsolvency(contextId, companyNumber, internalCompanyInsolvency);

        // then
        assertNull(existingDocument.getDeltaAt());
        verify(repository, Mockito.times(1)).save(Mockito.any());
        verify(insolvencyApiService, times(1)).invokeChsKafkaApi(eq(contextId), any(), eq(EventType.CHANGED));
    }

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
    void when_connection_issue_in_db_then_throw_bad_gateway_exception() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        assertThrows(BadGatewayException.class, () ->
                underTest.processInsolvency("436534543", "CH363453", companyInsolvency));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void when_insolvency_number_is_given_then_return_company_insolvency_information() {
        String companyNumber = "234234";

        InsolvencyDocument document = new InsolvencyDocument(companyNumber, new CompanyInsolvency(),
                OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), LocalDateTime.now(), "123");
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
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void when_invalid_insolvency_number_is_given_then_throw_exception() {
        Assert.assertThrows(RuntimeException.class, () -> underTest.retrieveCompanyInsolvency
                ("CH4000056"));

        verify(repository, Mockito.times(1)).findById(Mockito.any());
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void when_company_number_doesnt_exist_then_throws_DocumentNotFoundException_error() {
        String companyNumber = "CH363453";
        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.empty());

        Assert.assertThrows(DocumentNotFoundException.class, () ->
                underTest.deleteInsolvency(companyNumber, companyNumber));

        verify(repository, Mockito.times(0)).deleteById(Mockito.any());
        verify(repository, Mockito.times(1)).findById(companyNumber);
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void when_company_number_exist_then_finishes_successfully() {
        String companyNumber = "CH363453";
        String contextId = "1234";
        InsolvencyDocument document = new InsolvencyDocument(companyNumber, new CompanyInsolvency(),
                OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), LocalDateTime.now(), "123");
        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.of(document));

        underTest.deleteInsolvency(contextId, companyNumber);
        verify(repository, Mockito.times(1)).deleteById(Mockito.any());
        verify(repository, Mockito.times(1)).findById(companyNumber);
        verify(insolvencyApiService, times(1)).invokeChsKafkaApi(contextId, document, EventType.DELETED);
    }

    @Test
    void when_connection_issue_in_db_on_delete_then_throw_bad_gateway_exception() {
        String companyNumber = "CH363453";

        Mockito.when(repository.findById(companyNumber)).thenReturn(Optional.of(new InsolvencyDocument()));
        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .deleteById(companyNumber);

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency("436534543", companyNumber));
    }

    @Test
    void when_connection_issue_in_db_on_find_in_delete_then_throw_bad_gateway_exception() {
        String companyNumber = "CH363453";

        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .findById(companyNumber);

        assertThrows(BadGatewayException.class, () ->
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
