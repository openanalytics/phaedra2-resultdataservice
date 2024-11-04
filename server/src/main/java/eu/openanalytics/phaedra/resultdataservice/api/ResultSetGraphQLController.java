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
package eu.openanalytics.phaedra.resultdataservice.api;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import java.util.List;
import java.util.Optional;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ResultSetGraphQLController {

    private final ResultSetService resultSetService;

    public ResultSetGraphQLController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @QueryMapping
    public List<ResultSetDTO> resultSets(@Argument ResultSetFilter filter) {
        return resultSetService.getResultSets(filter);
    }

    @QueryMapping
    public ResultSetDTO resultSetById(@Argument long resultSetId) throws ResultSetNotFoundException {
        return resultSetService.getResultSetById(resultSetId);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetByIds(@Argument List<Long> resultSetIds) throws ResultSetNotFoundException {
        return resultSetService.getResultSetByIds(resultSetIds);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByPlateId(@Argument long plateId) throws ResultSetNotFoundException {
        return resultSetService.getResultSets(Optional.empty(), Optional.empty(), Optional.of(plateId));
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByPlateIds(@Argument List<Long> plateIds) throws ResultSetNotFoundException {
        return resultSetService.getResultSetsByPlateIds(plateIds);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByProtocolId(@Argument long protocolId) {
        return resultSetService.getResultSets(Optional.of(protocolId), Optional.empty(), Optional.empty());
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByProtocolIds(@Argument List<Long> protocolIds) {
        return resultSetService.getResultSetsByProtocolIds(protocolIds);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByMeasurementId(@Argument long measurementId) {
        return resultSetService.getResultSets(Optional.empty(), Optional.of(measurementId), Optional.empty());
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByMeasurementIds(@Argument List<Long> measurementIds) {
        return resultSetService.getResultSetsByMeasurementIds(measurementIds);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByPlateIdAndMeasurementId(@Argument long plateId, @Argument long measurementId) throws ResultSetNotFoundException {
        return resultSetService.getResultSets(Optional.empty(), Optional.of(measurementId), Optional.of(plateId));
    }

    @QueryMapping
    public ResultSetDTO latestResultSetByPlateId(@Argument long plateId) {
        try {
            return resultSetService.getLatestResultSetByPlateId(plateId, Optional.empty());
        } catch (ResultSetNotFoundException e) {
            return null;
        }
    }

    @QueryMapping
    public List<ResultSetDTO> latestResultSetsByPlateIds(@Argument List<Long> plateIds) {
        return resultSetService.getLatestResultSetsByPlateIds(plateIds);
    }
}
