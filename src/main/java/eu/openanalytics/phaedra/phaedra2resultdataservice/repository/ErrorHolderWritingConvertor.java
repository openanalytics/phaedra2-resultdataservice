package eu.openanalytics.phaedra.phaedra2resultdataservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.model.v2.runtime.ResultSet;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ErrorHolderWritingConvertor implements Converter<ResultSet.ErrorHolder, PGobject> {

    private final ObjectMapper objectMapper;

    public ErrorHolderWritingConvertor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public PGobject convert(ResultSet.ErrorHolder source) {
        PGobject result = new PGobject();
        result.setType("json");
        result.setValue(objectMapper.writeValueAsString(source.getErrors()));
        return result;
    }
}
