package uk.gov.companieshouse.insolvency.data.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.insolvency.data.converter.CompanyInsolvencyReadConverter;
import uk.gov.companieshouse.insolvency.data.converter.CompanyInsolvencyWriteConverter;
import uk.gov.companieshouse.insolvency.data.converter.EnumConverters;
import uk.gov.companieshouse.insolvency.data.converter.OffsetDateTimeReader;
import uk.gov.companieshouse.insolvency.data.converter.OffsetDateTimeWriter;
import uk.gov.companieshouse.insolvency.data.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.insolvency.data.serialization.LocalDateSerializer;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Configuration
public class ApplicationConfig {

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    /**
     * Bean to configure converters.
     * @return Mongodb custom converters mapping
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public InternalApiClient internalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

    /**
     * mongoCustomConversions.
     * @return MongoCustomConversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(new CompanyInsolvencyWriteConverter(objectMapper),
                new CompanyInsolvencyReadConverter(objectMapper),new EnumConverters.StringToEnum(),
                new EnumConverters.EnumToString(),new OffsetDateTimeWriter(),
                new OffsetDateTimeReader()));
    }

    private ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

}
