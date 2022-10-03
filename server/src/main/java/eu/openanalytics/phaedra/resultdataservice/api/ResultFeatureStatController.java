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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.util.dto.validation.OnCreate;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultFeatureStatNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultFeatureStatService;
import eu.openanalytics.phaedra.util.exceptionhandling.HttpMessageNotReadableExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.MethodArgumentNotValidExceptionHandler;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
public class ResultFeatureStatController implements MethodArgumentNotValidExceptionHandler, HttpMessageNotReadableExceptionHandler, UserVisibleExceptionHandler {

    private final ResultFeatureStatService resultFeatureStatService;

    public ResultFeatureStatController(ResultFeatureStatService resultFeatureStatService) {
        this.resultFeatureStatService = resultFeatureStatService;
    }

    @ResponseBody
    @PostMapping(path = "/resultset/{resultSetId}/resultfeaturestat", produces = {"application/json"}, consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public List<ResultFeatureStatDTO> createResultFeatureStat(@PathVariable long resultSetId, @RequestBody @Validated(OnCreate.class) ResultFeatureStatDTOList resultFeatureStatDTOList) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException, DuplicateResultFeatureStatException {
        return resultFeatureStatService.create(resultSetId, resultFeatureStatDTOList.list);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{resultSetId}/resultfeaturestat", produces = {"application/json"})
    public PageDTO<ResultFeatureStatDTO> getResultFeatureStat(@PathVariable long resultSetId,
                                                              @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                              @RequestParam(name = "pageSize", required = false) Optional<Integer> pageSize,
                                                              @RequestParam(name = "featureId", required = false) Integer featureId) throws ResultSetNotFoundException {
        Page<ResultFeatureStatDTO> pages;
        if (featureId == null) {
            pages = resultFeatureStatService.getPagedResultFeatureStats(resultSetId, page, pageSize);
        } else {
            pages = resultFeatureStatService.getPagedResultFeatureStatByFeatureId(resultSetId, featureId, page, pageSize);
        }
        return PageDTO.map(pages);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}", produces = {"application/json"})
    public ResultFeatureStatDTO getResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        return resultFeatureStatService.getResultFeatureStat(resultSetId, resultFeatureStatId);
    }

    @ResponseBody
    @PutMapping(path = "/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}", produces = {"application/json"})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HashMap<String, String> updateResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", "ResultFeatureStat cannot be updated (it can be deleted).");
        }};
    }

    @ResponseBody
    @DeleteMapping("/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) throws ResultSetNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException, ResultFeatureStatNotFoundException {
        resultFeatureStatService.delete(resultSetId, resultFeatureStatId);
    }

    /**
     * By default, Spring boot dus not support validating every object in a Collection using the {@link Validated} annotation.
     * The Java validation api support this using the {@link Valid} annotation, however, this gives a different kind of exception
     * which is not really useful to give a nice error to the API.
     * Therefore, we create a simple POJO that wraps the list and that can be used as parameter to a method.
     * In that a {@link MethodArgumentNotValidException} is thrown, which we can properly handle.
     */
    private static class ResultFeatureStatDTOList {
        @JsonValue
        @Valid
        private List<ResultFeatureStatDTO> list;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public ResultFeatureStatDTOList(ResultFeatureStatDTO... list) {
            this.list = Arrays.asList(list);
        }

        public List<ResultFeatureStatDTO> getList() {
            return list;
        }

        public void setList(List<ResultFeatureStatDTO> list) {
            this.list = list;
        }
    }

}
