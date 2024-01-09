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

import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultFeatureStatNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultFeatureStatService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ResultFeatureStatGraphQLController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler {

    private final ResultFeatureStatService resultFeatureStatService;

    public ResultFeatureStatGraphQLController(ResultFeatureStatService resultFeatureStatService) {
        this.resultFeatureStatService = resultFeatureStatService;
    }

    @QueryMapping
    public List<ResultFeatureStatDTO> resultSetFeatureStats(@Argument long resultSetId) throws ResultSetNotFoundException {
        List<ResultFeatureStatDTO> result = resultFeatureStatService.getResultSetFeatureStats(resultSetId);
        return result;
    }

    @QueryMapping
    public ResultFeatureStatDTO resultFeatureStat(@Argument long resultSetId, @Argument long resultFeatureStatId) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        ResultFeatureStatDTO result = resultFeatureStatService.getResultFeatureStat(resultSetId, resultFeatureStatId);
        return result;
    }
}
