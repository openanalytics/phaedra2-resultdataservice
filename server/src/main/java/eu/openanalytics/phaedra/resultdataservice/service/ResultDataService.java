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
package eu.openanalytics.phaedra.resultdataservice.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.FeatureDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.record.ResultDataFilter;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultDataRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

@Service
public class ResultDataService {

    private final ResultDataRepository resultDataRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ResultSetService resultSetService;

    private final DataSource dataSource;
    private final ProtocolServiceClient protocolServiceClient;
    private final Clock clock;
    private final ModelMapper modelMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;


    public ResultDataService(
    		ResultDataRepository resultDataRepository,
    		KafkaProducerService kafkaProducerService,
    		ResultSetService resultSetService,
    		DataSource dataSource, Clock clock, ModelMapper modelMapper,
        ProtocolServiceClient protocolServiceClient) {

        this.resultDataRepository = resultDataRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.resultSetService = resultSetService;
        this.dataSource = dataSource;
        this.clock = clock;
        this.modelMapper = modelMapper;
        this.protocolServiceClient = protocolServiceClient;
    }

    public ResultDataDTO create(long resultSetId, ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
        var resultSet = resultSetService.getResultSetById(resultSetId);

        if (resultSet.getOutcome() != StatusCode.SCHEDULED) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot add new ResultData to this set.");
        }

        ResultData resultData = modelMapper
            .map(resultDataDTO)
            .resultSetId(resultSetId)
            .createdTimestamp(LocalDateTime.now(clock))
            .build();

        resultDataDTO = save(resultData);
        kafkaProducerService.sendResultDataUpdated(resultDataDTO);
        return resultDataDTO;
    }

    public Page<ResultDataDTO> getPagedResultData(long resultSetId, int pageNumber, Optional<Integer> pageSize) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultDataRepository.findAllByResultSetId(PageRequest.of(pageNumber, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"), resultSetId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public List<ResultDataDTO> getResultDataByResultSetId(long resultSetId) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        List<ResultData> results = resultDataRepository.findAllByResultSetId(resultSetId);
        return results.stream().map(r -> modelMapper.map(r).build()).collect(Collectors.toList());
    }

    public ResultDataDTO getResultData(long resultSetId, long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }

        var res = resultDataRepository.findById(resultDataId);
        if (res.isEmpty()) {
            throw new ResultDataNotFoundException(resultDataId);
        }

        return modelMapper.map(res.get()).build();
    }

    public List<ResultDataDTO> getResultData(ResultDataFilter filter) {
        List<ResultData> results = new ArrayList<>();
        try {
            // In case protocolId list is not empty, we need to collect all respective featureIds
            if (isNotEmpty(filter.protocolIds())) {
                List<FeatureDTO> features = protocolServiceClient
                    .getFeaturesOfProtocols( filter.protocolIds());
                Set<Long> featureIds = features.stream()
                    .map(f -> f.getId()).collect(Collectors.toSet());
                // If filter also contains a non-empty list featureIds, we need to merge them with
                // featureIds fetched from protocolIds
                if (isNotEmpty(filter.featureIds())) {
                    featureIds.addAll(filter.featureIds());
                }
                // Create an updated result data filter objects that contains all the features the
                // client has asked for
                ResultDataFilter updatedFilter = new ResultDataFilter(
                    filter.resultDataIds(),
                    filter.resultSetIds(),
                    filter.protocolIds(),
                    new ArrayList<>(featureIds)
                );
                results.addAll(resultDataRepository.findAllByResultDataFilter(updatedFilter));
            } else {
                // In case the protocolIds
                results.addAll(resultDataRepository.findAllByResultDataFilter(filter));
            }
        } catch (ProtocolUnresolvableException e) {
            throw new RuntimeException(e);
        } finally {
            return results.stream()
                .map(resultData -> modelMapper.map(resultData).build())
                .toList();
        }
    }

    public ResultDataDTO getResultDataByResultSetIdAndFeatureId(long resultSetId, long featureId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }

        var result = resultDataRepository.findByResultSetIdAndFeatureId(resultSetId, featureId);
        if (result.isEmpty()) {
            throw new ResultDataNotFoundException(String.format("No resultData found for resultSetId %s and featureId %s", resultSetId, featureId));
        }

        return modelMapper.map(result.get()).build();
    }

    public List<ResultData> getResultDataByResultSetIds(List<Long> resultSetIds) throws ResultSetNotFoundException, ResultDataNotFoundException {
        return resultDataRepository.findByResultSetIdIn(resultSetIds);
    }

    public Page<ResultDataDTO> getPagedResultDataByFeatureId(long resultSetId, Integer featureId, Integer page, Optional<Integer> pageSize) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultDataRepository.findAllByResultSetIdAndFeatureId(PageRequest.of(page, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"), resultSetId, featureId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public List<ResultDataDTO> getResultDataByPlateIdAndProtocolIdAndFeatureId(long plateId, long protocolId, long featureId) {
        List<ResultData> result = resultDataRepository.findByPlateIdAndProtocolIdAndFeatureId(plateId, protocolId, featureId);
        return result.stream().map(r -> modelMapper.map(r).build()).toList();
    }

    public void delete(long resultSetId, long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException {
        var resultSet = resultSetService.getResultSetById(resultSetId);
        if (resultSet.getOutcome() != StatusCode.SCHEDULED) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot delete a ResultData from this set.");
        }
        var resultData = resultDataRepository.findById(resultDataId);
        if (resultData.isEmpty()) {
            throw new ResultDataNotFoundException(resultDataId);
        }
        if (resultData.get().getResultSetId() != resultSetId) {
            throw InvalidResultSetIdException.forResultData(resultSetId, resultDataId);
        }
        resultDataRepository.deleteById(resultDataId);
    }

    /**
     * Saves a {@link ResultData} and returns the resulting corresponding {@link ResultDataDTO}.
     */
    private ResultDataDTO save(ResultData resultData) {
        // workaround for https://github.com/spring-projects/spring-data-jdbc/issues/1033
        var simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("result_data").usingGeneratedKeyColumns("id");

        ResultData created = resultDataRepository.save(resultData);
        return modelMapper.map(created).build();

//        Number id = simpleJdbcInsert.executeAndReturnKey(new HashMap<>() {{
//            put("result_set_id", resultData.getResultSetId());
//            put("feature_id", resultData.getFeatureId());
//            put("values", resultData.getValues());
//            put("status_code", resultData.getStatusCode());
//            put("status_message", resultData.getStatusMessage());
//            put("exit_code", resultData.getExitCode());
//            put("created_timestamp", resultData.getCreatedTimestamp());
//        }});
//        return modelMapper.map(resultDataRepository.findById(id.longValue()).get()).build();
    }

}
