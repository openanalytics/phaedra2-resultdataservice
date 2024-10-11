package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.ErrorHolder;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolder;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.collections4.CollectionUtils;
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
    StringJoiner sql = new StringJoiner(" AND ", "SELECT * FROM result_set WHERE 1=1", "");
    MapSqlParameterSource parameters = new MapSqlParameterSource();

    if (CollectionUtils.isNotEmpty(filter.ids())) {
     sql.add("id in (:ids)");
     parameters.addValue("ids", filter.ids());
    }
    if (CollectionUtils.isNotEmpty(filter.protocolIds())) {
      sql.add("protocol_id in (:protocolIds)");
      parameters.addValue("protocolIds", filter.protocolIds());
    }
    if (CollectionUtils.isNotEmpty(filter.plateIds())) {
      sql.add("plate_id in (:plateIds)");
      parameters.addValue("plateIds", filter.plateIds());
    }
    if (CollectionUtils.isNotEmpty(filter.status())) {
      sql.add("outcome in (:status)");
      parameters.addValue("status", filter.status());
    }

    return namedParameterJdbcTemplate.query(sql.toString(), parameters, new ResultSetMapper());
  }

  private static class ResultSetMapper implements RowMapper<ResultSet> {
    @Override
    public ResultSet mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
      ResultSet resultSet = new ResultSet()
          .withId(rs.getLong("id"))
          .withProtocolId(rs.getLong("protocol_id"))
          .withPlateId(rs.getLong("plate_id"))
          .withMeasId(rs.getLong("meas_id"))
          .withExecutionStartTimeStamp(rs.getTimestamp("execution_start_time_stamp").toLocalDateTime())
          .withExecutionEndTimeStamp(rs.getTimestamp("execution_end_time_stamp").toLocalDateTime())
          .withErrors(rs.getObject("errors", ErrorHolder.class))
          .withErrorsText(rs.getString("errors_text"))
          .withOutcome(rs.getObject("outcome", StatusCodeHolder.class));
      return resultSet;
    }
  }
}
