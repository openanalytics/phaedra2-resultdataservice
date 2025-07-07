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
package eu.openanalytics.phaedra.resultdataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.lang.NonNull;

@Configuration
public class DataJdbcConfiguration extends AbstractJdbcConfiguration {

    private final ObjectMapper objectMapper;

    public DataJdbcConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
            new ResultSet.ErrorReadingConverter(objectMapper),
            new ResultSet.ErrorWritingConvertor(objectMapper),
            new ResultSet.StatusCodeHolderWritingConvertor(),
            new ResultSet.StatusCodeHolderReadingConvertor()));
    }
}
