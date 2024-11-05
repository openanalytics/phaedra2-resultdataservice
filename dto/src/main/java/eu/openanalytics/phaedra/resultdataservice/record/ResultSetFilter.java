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

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import java.util.List;

public record ResultSetFilter(
    List<Long> ids,
    List<Long> plateIds,
    List<Long> measurementIds,
    List<Long> protocolIds,
    List<StatusCode> status,
    Boolean mostRecentResultSetOnly
) {
}
