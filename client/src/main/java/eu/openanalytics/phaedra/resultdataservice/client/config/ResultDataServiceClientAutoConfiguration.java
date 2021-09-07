package eu.openanalytics.phaedra.resultdataservice.client.config;


import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.impl.HttpResultDataServiceClient;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResultDataServiceClientAutoConfiguration {

    @Bean
    public ResultDataServiceClient resultDataServiceClient(PhaedraRestTemplate phaedraRestTemplate) {
        return new HttpResultDataServiceClient(phaedraRestTemplate);
    }

}
