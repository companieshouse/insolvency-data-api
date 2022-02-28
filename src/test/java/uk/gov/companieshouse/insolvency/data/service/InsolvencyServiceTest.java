package uk.gov.companieshouse.insolvency.data.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.insolvency.InternalCompanyInsolvency;
import uk.gov.companieshouse.insolvency.data.InsolvencyRepository;

@ExtendWith(MockitoExtension.class)
public class InsolvencyServiceTest {

    @Mock
    private InsolvencyRepository repository;

    @InjectMocks
    private InsolvencyService underTest;

    @Test
    void when_insolvency_data_is_given_then_data_should_be_saved() {
        underTest.persist(new InternalCompanyInsolvency());

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any());
    }
}
