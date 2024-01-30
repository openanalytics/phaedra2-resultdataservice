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

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultDataService;
import eu.openanalytics.phaedra.util.dto.validation.OnCreate;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;

@RestController
@Validated
public class ResultDataController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler {

    private final ResultDataService resultDataService;

    public ResultDataController(ResultDataService resultDataService) {
        this.resultDataService = resultDataService;
    }

    @PostMapping("/resultsets/{resultSetId}/resultdata")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResultDataDTO createResultData(@PathVariable long resultSetId, @Validated(OnCreate.class) @RequestBody ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
        return resultDataService.create(resultSetId, resultDataDTO);
    }

    @GetMapping("/resultsets/{resultSetId}/resultdata")
    @ResponseBody
    public PageDTO<ResultDataDTO> getResultData(@PathVariable long resultSetId,
                                                @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                @RequestParam(name = "pageSize", required = false) Optional<Integer> pageSize,
                                                @RequestParam(name = "featureId", required = false) Integer featureId) throws ResultSetNotFoundException {
        Page<ResultDataDTO> pages;
        if (featureId == null) {
            pages = resultDataService.getPagedResultData(resultSetId, page, pageSize);
        } else {
            pages = resultDataService.getPagedResultDataByFeatureId(resultSetId, featureId, page, pageSize);
        }
        return PageDTO.map(pages);
    }

    @GetMapping("/resultsets/{resultSetId}/resultdata/{resultDataId}")
    @ResponseBody
    public ResultDataDTO getResultData(@PathVariable long resultSetId, @PathVariable long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        return resultDataService.getResultData(resultSetId, resultDataId);
    }

    @DeleteMapping("/resultsets/{resultSetId}/resultdata/{resultDataId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultData(@PathVariable long resultSetId, @PathVariable long resultDataId) throws ResultDataNotFoundException, ResultSetNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException {
        resultDataService.delete(resultSetId, resultDataId);
    }

}
