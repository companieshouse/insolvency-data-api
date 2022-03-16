package uk.gov.companieshouse.insolvency.data.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.insolvency.data.converter.CompanyInsolvencyConverter;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private final CompanyInsolvencyConverter insolvencyConverter;

    public ApplicationConfig(CompanyInsolvencyConverter insolvencyConverter) {
        this.insolvencyConverter = insolvencyConverter;
    }

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(insolvencyConverter));
    }

}
