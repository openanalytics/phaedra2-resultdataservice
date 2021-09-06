package eu.openanalytics.phaedra.resultdataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.lang.NonNull;

import java.util.List;

@Configuration
public class DataJdbcConfiguration extends AbstractJdbcConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @NonNull
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
            new ResultSet.ErrorReadingConverter(objectMapper),
            new ResultSet.ErrorWritingConvertor(objectMapper)));
    }
}
