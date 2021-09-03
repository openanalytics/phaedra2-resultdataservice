package eu.openanalytics.phaedra.phaedra2resultdataservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.model.v2.dto.ErrorDTO;
import eu.openanalytics.phaedra.model.v2.runtime.ResultSet;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Arrays;

@ReadingConverter
public class ErrorHolderReadingConvertor implements Converter<PGobject, ResultSet.ErrorHolder> {

    private final ObjectMapper objectMapper;

    public ErrorHolderReadingConvertor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public ResultSet.ErrorHolder convert(PGobject source) {
        var res = objectMapper.readValue(source.getValue(), ErrorDTO[].class);
        return new ResultSet.ErrorHolder(Arrays.asList(res));
    }

}
