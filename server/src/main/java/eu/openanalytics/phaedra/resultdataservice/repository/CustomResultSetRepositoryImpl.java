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
package eu.openanalytics.phaedra.resultdataservice.repository;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.ErrorReadingConverter;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolder;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolderReadingConvertor;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet.StatusCodeHolderWritingConvertor;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomResultSetRepositoryImpl implements CustomResultSetRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public CustomResultSetRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<ResultSet> findAllByResultSetFilter(ResultSetFilter filter) {
    if (filter == null) { logger.info("ResultSetFilter is null!! "); }
    String query = buildQuery(filter);
    MapSqlParameterSource params = buildParameters(filter);
    return namedParameterJdbcTemplate.query(query, params, new ResultSetMapper());
  }

  private String buildQuery(ResultSetFilter filter){
    StringBuilder queryBuilder = new StringBuilder("select rs.* from result_set rs");
    if (isTrue(filter.mostRecentResultSetOnly())) {
      queryBuilder.append(" join (select max(id) as max_id, protocol_id, plate_id, meas_id "
          + "      from result_set "
          + "      group by protocol_id, plate_id, meas_id) sub "
          + "on rs.protocol_id = sub.protocol_id "
          + "and rs.plate_id = sub.plate_id "
          + "and rs.meas_id = sub.meas_id "
          + "and rs.id = sub.max_id");
    }
    queryBuilder.append(" where 1=1");
    addFilters(queryBuilder, filter);
    return queryBuilder.toString();
  }

  private void addFilters(StringBuilder query, ResultSetFilter filter){
    if (isNotEmpty(filter.ids())) {
      query.append(" and rs.id in (:ids)");
    }
    if (isNotEmpty(filter.protocolIds())) {
      query.append(" and rs.protocol_id in (:protocolIds)");
    }
    if (isNotEmpty(filter.plateIds())) {
      query.append(" and rs.plate_id in (:plateIds)");
    }
    if (isNotEmpty(filter.measurementIds())) {
      query.append(" and rs.meas_id in (:measIds)");
    }
    if (isNotEmpty(filter.status())) {
      query.append(" and rs.outcome in (:status)");
    }
  }

  private MapSqlParameterSource buildParameters(ResultSetFilter filter){
    MapSqlParameterSource params = new MapSqlParameterSource();
    if (isNotEmpty(filter.ids())) {
      params.addValue("ids", filter.ids());
    }
    if (isNotEmpty(filter.protocolIds())) {
      params.addValue("protocolIds", filter.protocolIds());
    }
    if (isNotEmpty(filter.plateIds())) {
      params.addValue("plateIds", filter.plateIds());
    }
    if (isNotEmpty(filter.measurementIds())) {
      params.addValue("measIds", filter.measurementIds());
    }
    if (isNotEmpty(filter.status())) {
      StatusCodeHolderWritingConvertor statusCodeConvertor = new StatusCodeHolderWritingConvertor();
      params.addValue("status", filter.status().stream()
          .map(statusCode -> statusCodeConvertor.convert(new StatusCodeHolder(statusCode)))
          .toList());
    }
    return params;
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
//          .withErrors(errorReadingConverter.convert((PGobject) rs.getObject("errors")))
//          .withErrorsText(rs.getString("errors_text"))
          .withOutcome(statusCodeHolderReadingConvertor.convert(rs.getString("outcome")));
      return resultSet;
    }
  }
}
