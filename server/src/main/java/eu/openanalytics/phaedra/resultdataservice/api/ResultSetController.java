/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import eu.openanalytics.phaedra.util.dto.validation.OnCreate;
import eu.openanalytics.phaedra.util.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentTypeMismatchExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;

@RestController
@Validated
@RequestMapping("/resultsets")
public class ResultSetController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler, MethodArgumentTypeMismatchExceptionHandler {

    private final ResultSetService resultSetService;

    public ResultSetController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResultSetDTO createResultSet(@Validated(OnCreate.class) @RequestBody ResultSetDTO resultSetDTO) {
        return resultSetService.create(resultSetDTO);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResultSetDTO updateResultSet(@Validated(OnUpdate.class) @RequestBody ResultSetDTO resultSetDTO, @PathVariable Long id) throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
        return resultSetService.updateOutcome(resultSetDTO.withId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        resultSetService.delete(id);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResultSetDTO getResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        return resultSetService.getResultSetById(id);
    }

    @GetMapping
    @ResponseBody
    public PageDTO<ResultSetDTO> getResultSets(
    		@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
    		@RequestParam(name = "pageSize", required = false) Optional<Integer> pageSize,
    		@RequestParam(name = "plateId", required = false) Long plateId,
    		@RequestParam(name = "outcome", required = false) @Valid StatusCode outcome) {
    	return PageDTO.map(resultSetService.getPagedResultSets(plateId, outcome, page, pageSize));
    }

    @GetMapping("/latest")
    @ResponseBody
    public List<ResultSetDTO> getNMostRecentResultSets(
    		@RequestParam(name = "n", required = false, defaultValue = "10") Integer n,
    		@RequestParam(name = "plateId", required = false) Long plateId,
    		@RequestParam(name = "measId", required = false) Long measId) {
    	
        return resultSetService.getTopNResultsSets(n, plateId, measId);
    }
}
