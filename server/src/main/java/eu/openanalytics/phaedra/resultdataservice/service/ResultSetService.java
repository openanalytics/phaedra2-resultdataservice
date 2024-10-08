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

import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultSetRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;


@Service
public class ResultSetService {

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final ResultSetRepository resultSetRepository;
  private final KafkaProducerService kafkaProducerService;
  private final Clock clock;
  private final ModelMapper modelMapper;

  public ResultSetService(ResultSetRepository resultSetRepository,
      KafkaProducerService kafkaProducerService, Clock clock, ModelMapper modelMapper) {
    this.resultSetRepository = resultSetRepository;
    this.kafkaProducerService = kafkaProducerService;
    this.clock = clock;
    this.modelMapper = modelMapper;
  }

  /**
   * Creates a new ResultSet and saves it to the repository. Also sends an update message to the
   * Kafka topic.
   *
   * @param resultSetDTO The data transfer object containing the details of the ResultSet to be
   *                     created.
   * @return The saved ResultSetDTO with updated fields such as executionStartTimeStamp and outcome.
   */
  public ResultSetDTO create(ResultSetDTO resultSetDTO) {
    var resultSet = modelMapper.map(resultSetDTO)
        .executionStartTimeStamp(LocalDateTime.now(clock))
        .outcome(StatusCode.SCHEDULED)
        .build();

    resultSetDTO = save(resultSet);
    kafkaProducerService.sendResultSetUpdated(resultSetDTO);
    return resultSetDTO;
  }

  /**
   * Updates the outcome of an existing ResultSet by its ID. This method updates the outcome and
   * completion timestamp of the resultSetDTO if the ResultSet is found and has not been completed
   * yet. Notifications about the update are sent via Kafka.
   *
   * @param resultSetDTO The data transfer object containing the details of the ResultSet to be
   *                     updated.
   * @return The updated ResultSetDTO with the new executionEndTimeStamp and outcome.
   * @throws ResultSetAlreadyCompletedException If the ResultSet has already been completed.
   * @throws ResultSetNotFoundException         If the ResultSet does not exist.
   */
  public ResultSetDTO updateOutcome(ResultSetDTO resultSetDTO)
      throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
    Optional<ResultSet> existingResultSet = resultSetRepository.findById(resultSetDTO.getId());
    if (existingResultSet.isEmpty()) {
      throw new ResultSetNotFoundException(resultSetDTO.getId());
    }
    if (existingResultSet.get().getOutcome() != StatusCode.SCHEDULED
        || existingResultSet.get().getExecutionEndTimeStamp() != null) {
      throw new ResultSetAlreadyCompletedException();
    }

    ResultSet resultSet = modelMapper.map(resultSetDTO, existingResultSet.get())
        .executionEndTimeStamp(LocalDateTime.now(clock))
        .build();

    resultSetDTO = save(resultSet);
    kafkaProducerService.sendResultSetUpdated(resultSetDTO);
    return resultSetDTO;
  }

  /**
   * Deletes a ResultSet identified by the given ID. If the ResultSet does not exist, a
   * ResultSetNotFoundException is thrown.
   *
   * @param id The ID of the ResultSet to be deleted.
   * @throws ResultSetNotFoundException If the ResultSet with the specified ID is not found.
   */
  public void delete(Long id) throws ResultSetNotFoundException {
    Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
    if (existingResultSet.isEmpty()) {
      throw new ResultSetNotFoundException(id);
    }
    resultSetRepository.deleteById(id);
  }

  /**
   * Retrieves a ResultSet by the provided ID.
   *
   * @param id The ID of the ResultSet to retrieve.
   * @return The corresponding ResultSetDTO of the retrieved ResultSet.
   * @throws ResultSetNotFoundException If no ResultSet is found with the given ID.
   */
  public ResultSetDTO getResultSetById(Long id) throws ResultSetNotFoundException {
    Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
    if (existingResultSet.isEmpty()) {
      throw new ResultSetNotFoundException(id);
    }
    return modelMapper.map(existingResultSet.get()).build();
  }

  /**
   * Retrieves a list of ResultSetDTOs based on the provided list of IDs. If no ResultSets are
   * found, an empty list is returned.
   *
   * @param ids A list of IDs for which the corresponding ResultSetDTOs need to be fetched.
   * @return A list of ResultSetDTOs corresponding to the given IDs.
   */
  public List<ResultSetDTO> getResultSetByIds(List<Long> ids) {
      return fetchAndMapResultSets(() -> resultSetRepository.findByIds(ids));
  }

  /**
   * Retrieves a list of ResultSetDTOs based on the provided optional filters: protocol ID,
   * measurement ID, and plate ID. The method applies the filters if they are present and
   * fetches the corresponding result sets from the data source.
   *
   * @param protocolId An optional ID of the protocol to filter the result sets.
   * @param measurementId An optional ID of the measurement to filter the result sets.
   * @param plateId An optional ID of the plate to filter the result sets.
   * @return A list of ResultSetDTOs that match the specified criteria.
   */
  public List<ResultSetDTO> getResultSets(Optional<Long> protocolId, Optional<Long> measurementId, Optional<Long> plateId) {
    return findResultSets(protocolId, measurementId, plateId);
  }

  /**
   * Retrieves a list of ResultSetDTO based on the provided plate ID and an optional measurement
   * ID.
   *
   * @param plateId The ID of the plate for which the result sets are to be retrieved.
   * @param measId  An optional ID of the measurement to filter the result sets.
   * @return A list of ResultSetDTO matching the specified criteria.
   */
  public List<ResultSetDTO> getResultSetsByPlateId(Long plateId, Optional<Long> measId) {
    return findResultSets(Optional.empty(), measId, Optional.of(plateId));
  }

  /**
   * Retrieves a list of ResultSetDTO objects based on the provided list of plate IDs.
   *
   * @param plateIds A list of plate IDs for which the corresponding ResultSetDTOs are to be fetched.
   * @return A list of ResultSetDTOs corresponding to the given plate IDs.
   */
  public List<ResultSetDTO> getResultSetsByPlateIds(List<Long> plateIds) {
    return fetchAndMapResultSets(() -> resultSetRepository.findAllByPlateIds(plateIds));
  }

  /**
   * Retrieves a list of ResultSetDTOs based on the provided list of protocol IDs.
   * If no ResultSets are found, an empty list is returned.
   *
   * @param protocolIds A list of protocol IDs for which the corresponding ResultSetDTOs need to be fetched.
   * @return A list of ResultSetDTOs corresponding to the given protocol IDs.
   */
  public List<ResultSetDTO> getResultSetsByProtocolIds(List<Long> protocolIds) {
    return fetchAndMapResultSets(() -> resultSetRepository.findAllByProtocolIds(protocolIds));
  }

  /**
   * Retrieves a list of ResultSetDTO objects based on the provided list of measurement IDs.
   *
   * @param measurementIds A list of measurement IDs for which the corresponding ResultSetDTOs are to be fetched.
   * @return A list of ResultSetDTOs corresponding to the given measurement IDs.
   */
  public List<ResultSetDTO> getResultSetsByMeasurementIds(List<Long> measurementIds) {
    return fetchAndMapResultSets(() -> resultSetRepository.findAllByMeasurementIds(measurementIds));
  }

  /**
   * Retrieves the latest ResultSetDTO by the provided plate ID and an optional measurement ID.
   *
   * @param plateId The ID of the plate for which the latest result set is to be retrieved.
   * @param measurementId  An optional ID of the measurement to filter the result sets.
   * @return The latest ResultSetDTO matching the specified criteria, or null if no result set is
   * found.
   * @throws ResultSetNotFoundException If no result sets are found for the given criteria.
   */
  public ResultSetDTO getLatestResultSetByPlateId(Long plateId, Optional<Long> measurementId)
      throws ResultSetNotFoundException {
    List<ResultSetDTO> resultSets = findLatestResultSets(Optional.empty(), measurementId, Optional.of(plateId));
    return CollectionUtils.isNotEmpty(resultSets) ? resultSets.get(0) : null;
  }

  /**
   * Retrieves the latest Result Sets for a given Plate ID. The retrieval can be further filtered by
   * optional Measurement ID and Protocol ID.
   *
   * @param plateId    The ID of the plate for which the latest result sets are to be retrieved.
   * @param measId     An optional ID of the measurement to filter the result sets.
   * @param protocolId An optional ID of the protocol to filter the result sets.
   * @return A list of ResultSetDTO matching the specified criteria.
   */
  public List<ResultSetDTO> getLatestResultSetsByPlateId(Long plateId, Optional<Long> measId,
      Optional<Long> protocolId) {
    return findLatestResultSets(protocolId, measId, Optional.of(plateId));
  }

  /**
   * Retrieves the latest ResultSetDTO objects based on the provided list of plate IDs.
   *
   * @param plateIds A list of plate IDs for which the latest corresponding ResultSetDTOs are to be fetched.
   * @return A list of the latest ResultSetDTOs corresponding to the given plate IDs.
   */
  public List<ResultSetDTO> getLatestResultSetsByPlateIds(List<Long> plateIds) {
    return fetchAndMapResultSets(() -> resultSetRepository.findLatestByPlateIds(plateIds));
  }

  /**
   * Checks if a ResultSet with the specified ID exists in the repository.
   *
   * @param resultSetId The ID of the ResultSet to check for existence.
   * @return true if the ResultSet exists, false otherwise.
   */
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

  /**
   * Retrieves the top N {@link ResultSetDTO} based on the given filters.
   *
   * @param n          The number of top result sets to retrieve.
   * @param plateId    An optional ID of the plate for filtering the result sets.
   * @param measId     An optional ID of the measurement for filtering the result sets.
   * @param protocolId An optional ID of the protocol for filtering the result sets.
   * @return A list of {@link ResultSetDTO} matching the specified criteria.
   */
  public List<ResultSetDTO> getTopNResultSets(Integer n, Optional<Long> plateId,
      Optional<Long> measId, Optional<Long> protocolId) {
    return fetchNLatestResultSets(n, plateId, measId, protocolId);

  }

  private List<ResultSetDTO> findResultSets(Optional<Long> protocolId, Optional<Long> measurementId,
      Optional<Long> plateId) {
    if (protocolId.isPresent() && measurementId.isPresent() && plateId.isPresent()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findAllByProtocolIdAndMeasurementIdAndPlateId(
          protocolId.get(), measurementId.get(), plateId.get()));
    }
    if (protocolId.isPresent() && measurementId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByProtocolIdAndMeasurementId(protocolId.get(),
          measurementId.get()));
    }
    if (protocolId.isPresent() && plateId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByProtocolIdAndPlateId(protocolId.get(),
          plateId.get()));
    }
    if (measurementId.isPresent() && plateId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByMeasurementIdAndPlateId(measurementId.get(),
          plateId.get()));
    }
    if (protocolId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByProtocolId(protocolId.get()));
    }
    if (measurementId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByMeasurementId(measurementId.get()));
    }
    if (plateId.isPresent()) {
        return fetchAndMapResultSets(() -> resultSetRepository.findAllByPlateId(plateId.get()));
    }
    return fetchAndMapResultSets(() -> resultSetRepository.findAll());
  }

  private List<ResultSetDTO> findLatestResultSets(Optional<Long> protocolId, Optional<Long> measurementId,
      Optional<Long> plateId) {
    if (plateId.isPresent() && measurementId.isPresent() && protocolId.isPresent()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByPlateIdAndProtocolIdAndMeasId(plateId.get(),
          protocolId.get(), measurementId.get()));
    }
    if (plateId.isPresent() && measurementId.isEmpty() && protocolId.isEmpty()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByPlateId(plateId.get()));
    }
    if (plateId.isEmpty() && measurementId.isPresent() && protocolId.isEmpty()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByMeasId(measurementId.get()));
    }
    if (plateId.isEmpty() && measurementId.isEmpty() && protocolId.isPresent()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByProtocolId(protocolId.get()));
    }
    if (plateId.isPresent() && measurementId.isPresent() && protocolId.isEmpty()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByPlateIdAndMeasId(plateId.get(), measurementId.get()));
    }
    if (plateId.isPresent() && measurementId.isEmpty() && protocolId.isPresent()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByPlateIdAndProtocolId(plateId.get(),
          protocolId.get()));
    }
    if (plateId.isEmpty() && measurementId.isPresent() && protocolId.isPresent()) {
      return fetchAndMapResultSets(() -> resultSetRepository.findLatestByMeasIdAndProtocolId(measurementId.get(),
          protocolId.get()));
    }
    return Collections.emptyList();
  }

  private List<ResultSetDTO> fetchNLatestResultSets(Integer n, Optional<Long> plateId, Optional<Long> measId,
      Optional<Long> protocolId) {
    List<ResultSetDTO> results = findResultSets(protocolId, measId, plateId).subList(0, n);
    return CollectionUtils.isNotEmpty(results) ? results : Collections.emptyList();
  }

  private List<ResultSetDTO> fetchAndMapResultSets(Supplier<List<ResultSet>> fetcher) {
    List<ResultSet> results = fetcher.get();
    return mapResultSetsToDTOs(results);
  }

  private List<ResultSetDTO> mapResultSetsToDTOs(List<ResultSet> resultSets) {
    return CollectionUtils.isNotEmpty(resultSets) ? resultSets.stream()
        .map(resultSet -> modelMapper.map(resultSet).build())
        .toList() : Collections.emptyList();
  }
}
