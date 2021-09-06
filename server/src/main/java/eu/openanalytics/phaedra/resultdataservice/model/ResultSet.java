package eu.openanalytics.phaedra.resultdataservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.With;
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

    String outcome;

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

}
