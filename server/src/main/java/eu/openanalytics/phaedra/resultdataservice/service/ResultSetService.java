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
package eu.openanalytics.phaedra.resultdataservice.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultSetRepository;


@Service
public class ResultSetService {

    private final ResultSetRepository resultSetRepository;
    private final KafkaProducerService kafkaProducerService;

    private final Clock clock;
    private final ModelMapper modelMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;

    public ResultSetService(ResultSetRepository resultSetRepository, KafkaProducerService kafkaProducerService, Clock clock, ModelMapper modelMapper) {
        this.resultSetRepository = resultSetRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.clock = clock;
        this.modelMapper = modelMapper;
    }

    public ResultSetDTO create(ResultSetDTO resultSetDTO) {
        var resultSet = modelMapper.map(resultSetDTO)
            .executionStartTimeStamp(LocalDateTime.now(clock))
            .outcome(StatusCode.SCHEDULED)
            .build();

        resultSetDTO = save(resultSet);
        kafkaProducerService.sendResultSetUpdated(resultSetDTO);
        return resultSetDTO;
    }

    public ResultSetDTO updateOutcome(ResultSetDTO resultSetDTO) throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(resultSetDTO.getId());
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(resultSetDTO.getId());
        }
        if (existingResultSet.get().getOutcome() != StatusCode.SCHEDULED || existingResultSet.get().getExecutionEndTimeStamp() != null) {
            throw new ResultSetAlreadyCompletedException();
        }

        ResultSet resultSet = modelMapper.map(resultSetDTO, existingResultSet.get())
            .executionEndTimeStamp(LocalDateTime.now(clock))
            .build();

        resultSetDTO = save(resultSet);
        kafkaProducerService.sendResultSetUpdated(resultSetDTO);
        return resultSetDTO;
    }

    public void delete(Long id) throws ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(id);
        }
        resultSetRepository.deleteById(id);
    }

    public ResultSetDTO getResultSetById(Long id) throws ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(id);
        }
        return modelMapper.map(existingResultSet.get()).build();
    }


    /**
     * Gets all ResultSets for a given plate, optionally filtering on a measurement.
     * @param plateId
     * @param measId
     * @return
     * @throws ResultSetNotFoundException
     */
    public List<ResultSetDTO> getResultSetsByPlateId(Long plateId, Optional<Long> measId) throws ResultSetNotFoundException {
        List<ResultSet> resultSets;
        if (measId.isEmpty()) {
            resultSets = resultSetRepository.findAllByPlateId(plateId);
        } else {
            resultSets = resultSetRepository.findByPlateIdAndMeasId(plateId, measId.get());
        }
        return resultSets.stream().map(it -> modelMapper.map(it).build()).toList();
    }

    public ResultSetDTO getLatestResultSetByPlateId(Long plateId, Optional<Long> measId) throws ResultSetNotFoundException {
        List<ResultSet> resultSets;
        if (measId.isEmpty()) {
            resultSets = resultSetRepository.findLatestByPlateId(plateId);
        } else {
            resultSets = resultSetRepository.findLatestByPlateIdAndMeasId(plateId, measId.get());
        }
        return CollectionUtils.isNotEmpty(resultSets) ? modelMapper.map(resultSets.get(0)).build() : null;
    }

    public List<ResultSetDTO> getLatestResultSetsByPlateId(Long plateId, Optional<Long> measId, Optional<Long> protocolId) throws ResultSetNotFoundException {
        List<ResultSet> resultSets;
        if (measId.isPresent() && protocolId.isEmpty())
            resultSets = resultSetRepository.findLatestByPlateIdAndMeasId(plateId, measId.get());
        else if (protocolId.isPresent() && measId.isEmpty())
            resultSets = resultSetRepository.findLatestByPlateIdAndProtocolId(plateId, protocolId.get());
        else if (protocolId.isPresent() && measId.isPresent())
            resultSets = resultSetRepository.findLatestByPlateIdAndProtocolIdAndMeasId(plateId, protocolId.get(), measId.get());
        else
            resultSets = resultSetRepository.findLatestByPlateId(plateId);

         return resultSets.stream().map(it -> modelMapper.map(it).build()).toList();
    }

    public Page<ResultSetDTO> getPagedResultSets(Long plateId, StatusCode outcome, int pageNumber, Optional<Integer> pageSize) {
        Page<ResultSet> res;
        if (plateId != null) {
        	try {
				return new PageImpl<>(getResultSetsByPlateId(plateId, Optional.empty()));
			} catch (ResultSetNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
        } else if (outcome != null) {
        	res = resultSetRepository.findAllByOutcome(PageRequest.of(pageNumber, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"), outcome);
        } else {
            res = resultSetRepository.findAll(PageRequest.of(pageNumber, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"));
        }
        return res.map((r) -> (modelMapper.map(r).build()));
    }

    public boolean exists(long resultSetId) {
        return resultSetRepository.existsById(resultSetId);
    }

    /**
     * Saves a {@link ResultSet} and returns the resulting corresponding {@link ResultSetDTO}.
     */
    private ResultSetDTO save(ResultSet resultSet) {
        ResultSet newResultSet = resultSetRepository.save(resultSet);
        return modelMapper.map(newResultSet).build();
    }

    public List<ResultSetDTO> getTopNResultsSets(Integer n, Optional<Long> plateId, Optional<Long> measId, Optional<Long> protocolId) {
        List<ResultSet> result;

        // All 3 present
        if (plateId.isPresent() && measId.isPresent() && protocolId.isPresent())
            result = resultSetRepository.findLatestByPlateIdAndProtocolIdAndMeasId(plateId.get(), protocolId.get(), measId.get());
        // Only plateId present
        else if (plateId.isPresent() && measId.isEmpty() && protocolId.isEmpty())
            result = resultSetRepository.findLatestByPlateId(plateId.get());
        // Only measId present
        else if (plateId.isEmpty() && measId.isPresent() && protocolId.isEmpty())
            result = resultSetRepository.findLatestByMeasId(measId.get());
        // Only protocolId present
        else if (plateId.isEmpty() && measId.isEmpty() && protocolId.isPresent())
            result = resultSetRepository.findLatestByProtocolId(protocolId.get());
        // Only PlateId and MeasId present
        else if (plateId.isPresent() && measId.isPresent() && protocolId.isEmpty())
            result = resultSetRepository.findLatestByPlateIdAndMeasId(plateId.get(), measId.get());
        // Only PlateId and ProtocolId present
        else if (plateId.isPresent() && measId.isEmpty() && protocolId.isPresent())
            result = resultSetRepository.findLatestByPlateIdAndProtocolId(plateId.get(), protocolId.get());
        // Only MeasId and ProtocolId present
        else if (plateId.isEmpty() && measId.isPresent() && protocolId.isPresent())
            result = resultSetRepository.findLatestByMeasIdAndProtocolId(measId.get(), protocolId.get());
        else
            result = resultSetRepository.findNMostRecentResultSets(n);

        return result.stream().map(rs -> modelMapper.map(rs).build()).toList();
    }
}
