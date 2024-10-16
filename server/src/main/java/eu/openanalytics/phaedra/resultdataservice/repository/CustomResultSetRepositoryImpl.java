package eu.openanalytics.phaedra.resultdataservice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.ErrorReadingConverter;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolder;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolderReadingConvertor;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolderWritingConvertor;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomResultSetRepositoryImpl implements CustomResultSetRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public CustomResultSetRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<ResultSet> findAllByResultSetFilter(ResultSetFilter filter) {
    StringBuilder query = new StringBuilder("select * from result_set where 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (CollectionUtils.isNotEmpty(filter.ids())) {
     query.append(" and id in (:ids)");
     params.addValue("ids", filter.ids());
    }
    if (CollectionUtils.isNotEmpty(filter.protocolIds())) {
      query.append(" and protocol_id in (:protocolIds)");
      params.addValue("protocolIds", filter.protocolIds());
    }
    if (CollectionUtils.isNotEmpty(filter.plateIds())) {
      query.append(" and plate_id in (:plateIds)");
      params.addValue("plateIds", filter.plateIds());
    }
    if (CollectionUtils.isNotEmpty(filter.measurementIds())) {
      query.append(" and meas_id in (:measIds)");
      params.addValue("measIds", filter.measurementIds());
    }
    if (CollectionUtils.isNotEmpty(filter.status())) {
      StatusCodeHolderWritingConvertor statusCodeHolderWritingConvertor = new StatusCodeHolderWritingConvertor();
      query.append(" and outcome in (:status)");
      params.addValue("status", filter.status().stream()
          .map(statusCode -> statusCodeHolderWritingConvertor
              .convert(new StatusCodeHolder(statusCode))).toList());
    }

    return namedParameterJdbcTemplate.query(query.toString(), params, new ResultSetMapper());
  }

  private static class ResultSetMapper implements RowMapper<ResultSet> {
    @Override
    public ResultSet mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
      ErrorReadingConverter errorReadingConverter = new ErrorReadingConverter(new ObjectMapper());
      StatusCodeHolderReadingConvertor statusCodeHolderReadingConvertor = new StatusCodeHolderReadingConvertor();
      ResultSet resultSet = new ResultSet()
          .withId(rs.getLong("id"))
          .withProtocolId(rs.getLong("protocol_id"))
          .withPlateId(rs.getLong("plate_id"))
          .withMeasId(rs.getLong("meas_id"))
          .withExecutionStartTimeStamp(ObjectUtils.isNotEmpty(rs.getTimestamp("execution_start_time_stamp")) ? rs.getTimestamp("execution_start_time_stamp").toLocalDateTime() : null)
          .withExecutionEndTimeStamp(ObjectUtils.isNotEmpty(rs.getTimestamp("execution_end_time_stamp")) ? rs.getTimestamp("execution_end_time_stamp").toLocalDateTime() : null)
          .withErrors(errorReadingConverter.convert((PGobject) rs.getObject("errors")))
          .withErrorsText(rs.getString("errors_text"))
          .withOutcome(statusCodeHolderReadingConvertor.convert(rs.getString("outcome")));
      return resultSet;
    }
  }
}
