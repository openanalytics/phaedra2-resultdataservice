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
package eu.openanalytics.phaedra.resultdataservice.api;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultFeatureStatNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.FeatureStatService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import java.util.Optional;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FeatureStatsGraphQLController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler {

    private final FeatureStatService featureStatService;

    public FeatureStatsGraphQLController(FeatureStatService featureStatService) {
        this.featureStatService = featureStatService;
    }

    @QueryMapping
    public List<ResultFeatureStatDTO> resultSetFeatureStats(@Argument long resultSetId) throws ResultSetNotFoundException {
        List<ResultFeatureStatDTO> result = featureStatService.getResultSetFeatureStats(resultSetId);
        return result;
    }

    @QueryMapping
    public ResultFeatureStatDTO resultFeatureStat(@Argument long resultSetId, @Argument long resultFeatureStatId) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        ResultFeatureStatDTO result = featureStatService.getResultFeatureStat(resultSetId, resultFeatureStatId);
        return result;
    }

    @QueryMapping
    public List<ResultFeatureStatDTO> featureStatsByResultSetId(@Argument long resultSetId, @Argument String statName, @Argument List<String> wellTypes) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        List<ResultFeatureStatDTO> result = featureStatService.getResultFeatureStats(resultSetId, statName, wellTypes);
        return result;
    }
}
