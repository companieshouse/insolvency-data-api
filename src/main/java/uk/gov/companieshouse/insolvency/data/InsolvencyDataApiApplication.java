package uk.gov.companieshouse.insolvency.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class InsolvencyDataApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsolvencyDataApiApplication.class, args);
    }
}
