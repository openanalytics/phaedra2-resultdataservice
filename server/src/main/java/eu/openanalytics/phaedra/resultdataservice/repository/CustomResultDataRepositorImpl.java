package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.record.ResultDataFilter;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomResultDataRepositorImpl implements CustomResultDataRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public CustomResultDataRepositorImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<ResultData> findAllByResultDataFilter(ResultDataFilter filter) {
    StringBuilder query = new StringBuilder("select * from result_data where 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (CollectionUtils.isNotEmpty(filter.ids())) {
      query.append(" and id in (:ids)");
      params.addValue("ids", filter.ids());
    }
    if (CollectionUtils.isNotEmpty(filter.resultSetIds())) {
      query.append(" and result_set_id in (:resultSetIds)");
      params.addValue("resultSetIds", filter.resultSetIds());
    }
    if (CollectionUtils.isNotEmpty(filter.featureIds())) {
      query.append(" and feature_id in (:featureIds)");
      params.addValue("featureIds", filter.featureIds());
    }

//    if (CollectionUtils.isNotEmpty(filter.status())) {
//      query.append(" and status_code in (:status)");
//      params.addValue("status", filter.status().stream()
//          .map(statusCode -> statusCode.name).toList());
//    }

    return namedParameterJdbcTemplate.query(query.toString(), params, new ResultDataMapper());
  }

  private static class ResultDataMapper implements RowMapper<ResultData> {
    @Override
    public ResultData mapRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
      ResultData resultData = new ResultData()
          .withId(rs.getLong("id"))
          .withResultSetId(rs.getLong("protocol_id"))
          .withFeatureId(rs.getLong("plate_id"))
          .withValues(rs.getObject("values", float[].class))
          .withCreatedTimestamp(ObjectUtils.isNotEmpty(rs.getTimestamp("created_timestamp")) ? rs.getTimestamp("execution_end_time_stamp").toLocalDateTime() : null)
          .withStatusMessage(rs.getString("status_message"))
          .withStatusCode(rs.getObject("status_code", StatusCode.class));
      return resultData;
    }
  }
}
