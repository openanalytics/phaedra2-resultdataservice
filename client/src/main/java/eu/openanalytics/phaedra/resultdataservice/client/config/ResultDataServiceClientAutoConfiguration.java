/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.client.config;


import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceGraphQLClient;
import eu.openanalytics.phaedra.resultdataservice.client.impl.HttpResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.impl.ResultDataServiceGraphQLClientImpl;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ResultDataServiceClientAutoConfiguration {

    @Bean
    public ResultDataServiceClient resultDataServiceClient(PhaedraRestTemplate phaedraRestTemplate, IAuthorizationService authService, Environment environment) {
        return new HttpResultDataServiceClient(phaedraRestTemplate, authService, environment);
    }

    @Bean
    public ResultDataServiceGraphQLClient resultDataServiceGraphQLClient(IAuthorizationService authService, Environment environment) {
        return new ResultDataServiceGraphQLClientImpl(authService, environment);
    }
}
