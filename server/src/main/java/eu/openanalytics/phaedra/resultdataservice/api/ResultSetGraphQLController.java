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
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentTypeMismatchExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ResultSetGraphQLController {

    private final ResultSetService resultSetService;

    public ResultSetGraphQLController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @QueryMapping
    public ResultSetDTO resultSetById(@Argument long resultSetId) throws ResultSetNotFoundException {
        return resultSetService.getResultSetById(resultSetId);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByPlateId(@Argument long plateId) throws ResultSetNotFoundException {
        return resultSetService.getResultSets(null, null, Optional.of(plateId));
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByProtocolId(@Argument long protocolId) {
        return resultSetService.getResultSets(Optional.of(protocolId), null, null);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByMeasurementId(@Argument long measurementId) {
        return resultSetService.getResultSets(null, Optional.of(measurementId), null);
    }

    @QueryMapping
    public List<ResultSetDTO> resultSetsByPlateIdAndMeasurementId(@Argument long plateId, @Argument long measurementId) throws ResultSetNotFoundException {
        return resultSetService.getResultSets(null, Optional.of(measurementId), Optional.of(plateId));
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
