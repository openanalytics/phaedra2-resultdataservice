/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
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
package eu.openanalytics.phaedra.resultdataservice.client;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.record.ResultDataFilter;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import java.util.List;

public interface ResultDataServiceGraphQLClient {
  public ResultSetDTO getResultSet(long resultSetId);
  public List<ResultSetDTO> getResultSets(ResultSetFilter filter);
  public List<ResultSetDTO> getResultSetsByPlateId(Long plateId);
  public List<ResultSetDTO> getResultSetsByPlateIds(List<Long> plateIds);
  public List<ResultSetDTO> getResultSetsByMeasurementId(Long measurementId);
  public List<ResultSetDTO> getResultSetsByMeasurementIds(List<Long> measurementIds);
  public List<ResultSetDTO> getResultSetsByProtocolId(Long protocolId);
  public List<ResultSetDTO> getResultSetsByProtocolIds(List<Long> protocolIds);
  public List<ResultSetDTO> getResultSetsByStatus(StatusCode outcome);
  public List<ResultSetDTO> getResultSetsByStatus(List<StatusCode> outcomes);

  public List<ResultDataDTO> getResultData(ResultDataFilter filter);

}
