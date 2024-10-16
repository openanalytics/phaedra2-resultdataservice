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
package eu.openanalytics.phaedra.resultdataservice.record;

import java.util.List;

public record ResultDataFilter(
    List<String> ids,
    List<String> resultDataIds,
    List<String> resultSetIds,
    List<String> protocolIds,
    List<String> featureIds
) {
  public ResultDataFilter {
    // Ensure the lists are never null
    ids = (ids != null) ? ids : List.of();
    resultDataIds = (resultDataIds != null) ? resultDataIds : List.of();
    resultSetIds = (resultSetIds != null) ? resultSetIds : List.of();
    protocolIds = (protocolIds != null) ? protocolIds : List.of();
    featureIds = (featureIds != null) ? featureIds : List.of();
  }
}
