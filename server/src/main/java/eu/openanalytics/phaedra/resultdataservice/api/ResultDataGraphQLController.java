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

import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultDataService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ResultDataGraphQLController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler {

    private final ResultDataService resultDataService;

    public ResultDataGraphQLController(ResultDataService resultDataService) {
        this.resultDataService = resultDataService;
    }

    @QueryMapping
    public List<ResultDataDTO> resultDataByResultSetId(@Argument long resultSetId) throws ResultSetNotFoundException {
        return resultDataService.getResultDataByResultSetId(resultSetId);
    }

    @QueryMapping
    public ResultDataDTO resultDataByResultSetIdAndResultDataId(@Argument long resultSetId, @Argument long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        return resultDataService.getResultData(resultSetId, resultDataId);
    }

    @QueryMapping
    public ResultDataDTO resultDataByResultSetIdAndFeatureId(@Argument long resultSetId, @Argument long featureId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        return resultDataService.getResultDataByResultSetIdAndFeatureId(resultSetId, featureId);
    }

    @QueryMapping
    public List<ResultDataDTO> resultDataByPlateIdAndProtocolIdAndFeatureId(@Argument Long plateId, @Argument Long protocolId, @Argument Long featureId) {
        return resultDataService.getResultDataByPlateIdAndProtocolIdAndFeatureId(plateId, protocolId, featureId);
    }
}
