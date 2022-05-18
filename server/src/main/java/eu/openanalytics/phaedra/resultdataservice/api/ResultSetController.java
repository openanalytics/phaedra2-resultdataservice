/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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

import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentTypeMismatchExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@Validated
public class ResultSetController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler, MethodArgumentTypeMismatchExceptionHandler {

    private final ResultSetService resultSetService;

    public ResultSetController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @ResponseBody
    @PostMapping(path = "/resultset", produces = {"application/json"}, consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public ResultSetDTO createResultSet(@Validated(OnCreate.class) @RequestBody ResultSetDTO resultSetDTO) {
        return resultSetService.create(resultSetDTO);
    }

    @ResponseBody
    @PutMapping(path = "/resultset/{id}", produces = {"application/json"}, consumes = {"application/json"})
    public ResultSetDTO updateResultSet(@Validated(OnUpdate.class) @RequestBody ResultSetDTO resultSetDTO, @PathVariable Long id) throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
        return resultSetService.updateOutcome(resultSetDTO.withId(id));
    }

    @DeleteMapping("/resultset/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        resultSetService.delete(id);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{id}", produces = {"application/json"})
    public ResultSetDTO getResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        return resultSetService.getResultSetById(id);
    }

    @ResponseBody
    @GetMapping(path = "/resultset", produces = {"application/json"})
    public PageDTO<ResultSetDTO> getAllResultSets(@RequestParam Map<String, String> filters,
                                                  @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                  @RequestParam(name = "pageSize", required = false) Optional<Integer> pageSize,
                                                  @RequestParam(name = "outcome", required = false) @Valid StatusCode outcome) {
        if (MapUtils.isEmpty(filters)) {
            var pages = resultSetService.getPagedResultSets(page, outcome, pageSize);
            return PageDTO.map(pages);
        } else {
            var pages = resultSetService.getFilteredPagedResultSets(filters, page, outcome, pageSize);
            return PageDTO.map(pages);
        }
    }

    @ResponseBody
    @GetMapping(path = "/resultset/latest", produces = { "application/json" })
    public List<ResultSetDTO> getNMostRecentResultSets(@RequestParam(name = "n", required = false, defaultValue = "10") Integer n) {
        return resultSetService.getTopNResultsSets(n);
    }
}
