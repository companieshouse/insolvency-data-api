package uk.gov.companieshouse.insolvency.data.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import uk.gov.companieshouse.api.insolvency.CompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.api.insolvency.InternalData;
import uk.gov.companieshouse.insolvency.data.api.InsolvencyApiService;
import uk.gov.companieshouse.insolvency.data.common.EventType;
import uk.gov.companieshouse.insolvency.data.exceptions.BadGatewayException;
import uk.gov.companieshouse.insolvency.data.exceptions.ConflictException;
import uk.gov.companieshouse.insolvency.data.model.InsolvencyDocument;
import uk.gov.companieshouse.insolvency.data.repository.InsolvencyRepository;

@ExtendWith(MockitoExtension.class)
class InsolvencyServiceImplTest {

    private static final String DELTA_AT = "20221008152823383176";
    private static final String STALE_DELTA_AT = "20201008152823383176";
    private static final OffsetDateTime DB_DELTA_AT = OffsetDateTime.of(2021, 10, 31, 0, 0, 0, 0, ZoneOffset.UTC);
    private static final String COMPANY_NUMBER = "12345678";

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
        InternalCompanyInsolvency internalCompanyInsolvency = createInternalCompanyInsolvency();
        internalCompanyInsolvency.getInternalData().setDeltaAt(requestDeltaAt);

        InsolvencyDocument existingDocument = new InsolvencyDocument();
        existingDocument.setDeltaAt(existingDeltaAt);
        Optional<InsolvencyDocument> existingDocumentOptional = Optional.of(existingDocument);

        when(repository.findById(anyString())).thenReturn(existingDocumentOptional);

        // when
        Executable executable = () -> underTest.processInsolvency(COMPANY_NUMBER, internalCompanyInsolvency);

        // then
        assertThrows(ConflictException.class, executable);
        verifyNoInteractions(insolvencyApiService);
        verify(repository, times(0)).save(any());
    }

    @Test
    void should_process_successfully_when_existing_delta_at_is_null() {
        // given
        OffsetDateTime requestDeltaAt = OffsetDateTime.parse("2021-03-08T12:00:00.000Z");

        InternalCompanyInsolvency internalCompanyInsolvency = createInternalCompanyInsolvency();
        internalCompanyInsolvency.getInternalData().setDeltaAt(requestDeltaAt);
        InsolvencyDocument existingDocument = new InsolvencyDocument();

        when(repository.findById(anyString())).thenReturn(Optional.of(existingDocument));

        // when
        underTest.processInsolvency(COMPANY_NUMBER, internalCompanyInsolvency);

        // then
        assertNull(existingDocument.getDeltaAt());
        verify(repository).save(any());
        verify(insolvencyApiService).invokeChsKafkaApi(anyString(), any(), eq(EventType.CHANGED));
    }

    @Test
    void when_insolvency_data_is_given_then_data_should_be_saved() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        underTest.processInsolvency(COMPANY_NUMBER, companyInsolvency);

        verify(repository).save(any());
        verify(insolvencyApiService).invokeChsKafkaApi(anyString(), any(), eq(EventType.CHANGED));
    }

    @Test
    void shouldThrowBadGatewayWhenTransientDataAccessExCaughtOnFindDuringPut() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new TransientDataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.processInsolvency(COMPANY_NUMBER, companyInsolvency));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenDataAccessExCaughtOnFindDuringPut() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new DataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.processInsolvency(COMPANY_NUMBER, companyInsolvency));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenTransientDataAccessExCaughtOnSaveDuringPut() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new TransientDataAccessException("Connection broken") {
        })
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        assertThrows(BadGatewayException.class, () ->
                underTest.processInsolvency(COMPANY_NUMBER, companyInsolvency));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenDataAccessExCaughtOnSaveDuringPut() {
        InternalCompanyInsolvency companyInsolvency = createInternalCompanyInsolvency();

        doThrow(new DataAccessException("Connection broken") {
        })
                .when(repository)
                .save(isA(InsolvencyDocument.class));

        assertThrows(BadGatewayException.class, () ->
                underTest.processInsolvency(COMPANY_NUMBER, companyInsolvency));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenTransientDataAccessExCaughtOnFindDuringDelete() {
        doThrow(new TransientDataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenDataAccessExCaughtOnFindDuringDelete() {
        doThrow(new DataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void shouldThrowBadGatewayWhenTransientDataAccessExCaughtOnDeleteByIdDuringDelete() {
        when(repository.findById(anyString())).thenReturn(Optional.of(new InsolvencyDocument()));
        doThrow(new TransientDataAccessException("Connection broken") {
        })
                .when(repository)
                .deleteById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
    }

    @Test
    void shouldThrowBadGatewayWhenDataAccessExCaughtOnDeleteByIdDuringDelete() {
        when(repository.findById(anyString())).thenReturn(Optional.of(new InsolvencyDocument()));
        doThrow(new DataAccessException("Connection broken") {
        })
                .when(repository)
                .deleteById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
    }

    @Test
    void when_insolvency_number_is_given_then_return_company_insolvency_information() {
        InsolvencyDocument document = new InsolvencyDocument(COMPANY_NUMBER, new CompanyInsolvency(),
                OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), LocalDateTime.now(), "123");
        when(repository.findById(COMPANY_NUMBER)).thenReturn(Optional.of(document));

        CompanyInsolvency companyInsolvency = underTest.retrieveCompanyInsolvency(COMPANY_NUMBER);

        Assertions.assertThat(companyInsolvency).isNotNull();
        verify(repository).findById(any());

    }

    @Test
    void shouldThrowBadGatewayWhenTransientDataAccessExCaughtOnFindDuringGet() {
        doThrow(new TransientDataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.retrieveCompanyInsolvency("234234"));
    }

    @Test
    void shouldThrowBadGatewayWhenDataAccessExCaughtOnFindDuringGet() {
        doThrow(new DataAccessException("Connection broken") {
        })
                .when(repository)
                .findById(anyString());

        assertThrows(BadGatewayException.class, () ->
                underTest.retrieveCompanyInsolvency("234234"));
    }

    @Test
    void when_invalid_insolvency_number_is_given_then_throw_exception() {
        Assert.assertThrows(RuntimeException.class, () -> underTest.retrieveCompanyInsolvency(COMPANY_NUMBER));

        verify(repository).findById(any());
        verify(insolvencyApiService, times(0)).invokeChsKafkaApi(anyString(), any(), any());
    }

    @Test
    void shouldInvokeChsKafkaApiWithEmptyDataWhenDocumentDoesNotExistDuringDelete() {
        when(repository.findById(COMPANY_NUMBER)).thenReturn(Optional.empty());

        underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT);

        verify(repository).findById(COMPANY_NUMBER);
        verifyNoMoreInteractions(repository);
        verify(insolvencyApiService).invokeChsKafkaApi(COMPANY_NUMBER, null, EventType.DELETED);
    }

    @Test
    void when_company_number_exist_then_finishes_successfully() {
        CompanyInsolvency data = new CompanyInsolvency();
        InsolvencyDocument document = new InsolvencyDocument(COMPANY_NUMBER, data,
                OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), LocalDateTime.now(), "123");
        document.setDeltaAt(DB_DELTA_AT);
        when(repository.findById(COMPANY_NUMBER)).thenReturn(Optional.of(document));

        underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT);
        verify(repository).findById(COMPANY_NUMBER);
        verify(repository).deleteById(COMPANY_NUMBER);
        verify(insolvencyApiService).invokeChsKafkaApi(COMPANY_NUMBER, data, EventType.DELETED);
    }

    @Test
    void shouldThrowConflictExceptionWhenDocumentExistsAndRequestDeltaAtIsStale() {
        CompanyInsolvency data = new CompanyInsolvency();
        InsolvencyDocument document = new InsolvencyDocument(COMPANY_NUMBER, data,
                OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC), LocalDateTime.now(), "123");
        document.setDeltaAt(DB_DELTA_AT);
        when(repository.findById(COMPANY_NUMBER)).thenReturn(Optional.of(document));

        assertThrows(ConflictException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, STALE_DELTA_AT));
        verify(repository).findById(COMPANY_NUMBER);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(insolvencyApiService);
    }

    @Test
    void when_connection_issue_in_db_on_delete_then_throw_bad_gateway_exception() {
        when(repository.findById(COMPANY_NUMBER)).thenReturn(Optional.of(new InsolvencyDocument()));
        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .deleteById(COMPANY_NUMBER);

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
    }

    @Test
    void when_connection_issue_in_db_on_find_in_delete_then_throw_bad_gateway_exception() {
        doThrow(new DataAccessResourceFailureException("Connection broken"))
                .when(repository)
                .findById(COMPANY_NUMBER);

        assertThrows(BadGatewayException.class, () ->
                underTest.deleteInsolvency(COMPANY_NUMBER, DELTA_AT));
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
