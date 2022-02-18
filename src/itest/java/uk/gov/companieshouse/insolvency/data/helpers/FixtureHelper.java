package uk.gov.companieshouse.insolvency.data.helpers;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class FixtureHelper {

    public String readJsonFile (String filename) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/itest/java/uk/gov/companieshouse/insolvency/data/fixtures/" + filename + ".json")));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }

        return sb.toString();
    }

}
