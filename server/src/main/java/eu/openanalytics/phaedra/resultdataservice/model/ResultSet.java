/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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
package eu.openanalytics.phaedra.resultdataservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.With;
import lombok.experimental.Delegate;
import lombok.experimental.NonFinal;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Value
@Builder(toBuilder = true)
@With
@AllArgsConstructor
@NonFinal
public class ResultSet {

    @Id
    Long id;

    Long protocolId;

    Long plateId;

    Long measId;

    LocalDateTime executionStartTimeStamp;

    LocalDateTime executionEndTimeStamp;

    StatusCodeHolder outcome;

    public StatusCode getOutcome() {
        return outcome.getStatusCode();
    }

    public static class ResultSetBuilder {
        public ResultSetBuilder outcome(StatusCode outcome) {
            this.outcome = new StatusCodeHolder(outcome);
            return this;
        }

        // needed for lombok compatibility
        public ResultSetBuilder outcome(StatusCodeHolder outcome) {
            this.outcome = outcome;
            return this;
        }
    }

    ErrorHolder errors;

    String errorsText;

    @WritingConverter
    public static class ErrorWritingConvertor implements Converter<ErrorHolder, PGobject> {

        private final ObjectMapper objectMapper;

        public ErrorWritingConvertor(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @SneakyThrows
        @Override
        public PGobject convert(ErrorHolder source) {
            PGobject result = new PGobject();
            result.setType("json");
            result.setValue(objectMapper.writeValueAsString(source.getErrors()));
            return result;
        }
    }

    @ReadingConverter
    public static class ErrorReadingConverter implements Converter<PGobject, ErrorHolder> {

        private final ObjectMapper objectMapper;

        public ErrorReadingConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @SneakyThrows
        @Override
        public ErrorHolder convert(PGobject source) {
            var res = objectMapper.readValue(source.getValue(), ErrorDTO[].class);
            return new ErrorHolder(Arrays.asList(res));
        }
    }

    @Data
    @AllArgsConstructor
    public static class ErrorHolder {
        List<ErrorDTO> errors;
    }

    /**
     * Spring Data JDBC does not support using Enums in automatic generated WHERE clauses.
     * Therefore, we wrap the {@see StatusCode} enum in a custom type. This custom type
     * is then converted using the {@see StatusCodeReadingConvertor} and {@see StatusCodeWritingConvertor}.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusCodeHolder {
        @Delegate
        StatusCode statusCode;
    }

    @WritingConverter
    public static class StatusCodeHolderWritingConvertor implements Converter<ResultSet.StatusCodeHolder, PGobject> {
        @SneakyThrows
        @Override
        public PGobject convert(ResultSet.StatusCodeHolder source) {
            PGobject result = new PGobject();
            result.setType("status_code");
            result.setValue(source.name());
            return result;
        }
    }

    @ReadingConverter
    public static class StatusCodeHolderReadingConvertor implements Converter<String, ResultSet.StatusCodeHolder> {
        @SneakyThrows
        @Override
        public ResultSet.StatusCodeHolder convert(@NonNull String source) {
            return new ResultSet.StatusCodeHolder(StatusCode.valueOf(source));
        }
    }

}
