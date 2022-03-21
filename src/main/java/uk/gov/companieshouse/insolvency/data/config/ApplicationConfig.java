package uk.gov.companieshouse.insolvency.data.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.insolvency.data.converter.CaseDateTypeEnumReadingConverter;
import uk.gov.companieshouse.insolvency.data.converter.CompanyInsolvencyConverter;
import uk.gov.companieshouse.insolvency.data.converter.ModelTypeEnumReadingConverter;
import uk.gov.companieshouse.insolvency.data.converter.RoleEnumReadingConverter;
import uk.gov.companieshouse.insolvency.data.converter.StatusEnumReadingConverter;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private final CompanyInsolvencyConverter insolvencyConverter;
    private final ModelTypeEnumReadingConverter modelTypeEnumReadingConverter;
    private final CaseDateTypeEnumReadingConverter caseDateTypeEnumReadingConverter;
    private final RoleEnumReadingConverter roleEnumReadingConverter;
    private final StatusEnumReadingConverter statusEnumReadingConverter;

    /**
     * Configuring converters to save and read data from mongodb.
     * @param insolvencyConverter data type converter
     * @param modelTypeEnumReadingConverter model type enum converter
     * @param caseDateTypeEnumReadingConverter case data type enum converter
     * @param roleEnumReadingConverter role enum converter
     * @param statusEnumReadingConverter status enum converter
     */
    public ApplicationConfig(CompanyInsolvencyConverter insolvencyConverter,
                             ModelTypeEnumReadingConverter modelTypeEnumReadingConverter,
                             CaseDateTypeEnumReadingConverter caseDateTypeEnumReadingConverter,
                             RoleEnumReadingConverter roleEnumReadingConverter,
                             StatusEnumReadingConverter statusEnumReadingConverter) {
        this.insolvencyConverter = insolvencyConverter;
        this.modelTypeEnumReadingConverter = modelTypeEnumReadingConverter;
        this.caseDateTypeEnumReadingConverter = caseDateTypeEnumReadingConverter;
        this.roleEnumReadingConverter = roleEnumReadingConverter;
        this.statusEnumReadingConverter = statusEnumReadingConverter;
    }

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    /**
     * Bean to configure converters.
     * @return Mongodb custom converters mapping
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                insolvencyConverter,
                modelTypeEnumReadingConverter,
                caseDateTypeEnumReadingConverter,
                roleEnumReadingConverter,
                statusEnumReadingConverter));
    }

}
