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
package eu.openanalytics.phaedra.resultdataservice.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class PlateResultDTO {

    Map<Long, ResultsPerProtocolDTO> protocols;

    @Value
    @Builder
    public static class ResultsPerProtocolDTO {
        Map<Long, Collection<ResultsPerMeasurement>> measurements;
    }

    @Value
    @Builder
    public static class ResultsPerMeasurement {
        List<ResultDataDTO> resultData;
    }

}


