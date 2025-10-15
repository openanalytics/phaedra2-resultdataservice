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

import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.UnresolvableObjectException;
import eu.openanalytics.phaedra.plateservice.dto.PlateDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceGraphQlClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.ProtocolDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.record.FeatureData;
import eu.openanalytics.phaedra.resultdataservice.record.FeatureValue;
import eu.openanalytics.phaedra.resultdataservice.record.PlateData;
import eu.openanalytics.phaedra.resultdataservice.record.PlateResultSetData;
import eu.openanalytics.phaedra.resultdataservice.record.ProtocolData;
import eu.openanalytics.phaedra.resultdataservice.record.WellData;
import eu.openanalytics.phaedra.resultdataservice.service.ResultDataService;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PlateResultsGraphQLController {

  private final ResultSetService resultSetService;
  private final ResultDataService resultDataService;
  private final ProtocolServiceGraphQlClient protocolServiceGraphQlClient;
  private final PlateServiceClient plateServiceClient;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public PlateResultsGraphQLController(ResultSetService resultSetService,
      ResultDataService resultDataService,
      ProtocolServiceGraphQlClient protocolServiceGraphQlClient,
      PlateServiceClient plateServiceClient) {
    this.resultSetService = resultSetService;
    this.resultDataService = resultDataService;
    this.protocolServiceGraphQlClient = protocolServiceGraphQlClient;
    this.plateServiceClient = plateServiceClient;
  }

  @QueryMapping
  public PlateResultSetData latestPlateResultByPlateId(@Argument long plateId)
      throws ResultSetNotFoundException, UnresolvableObjectException, ProtocolUnresolvableException {
    ResultSetDTO resultSetDTO = resultSetService.getLatestResultSetByPlateId(plateId,
        Optional.empty());

    PlateDTO plateDTO = plateServiceClient.getPlate(plateId);
    List<WellDTO> wellDTOs = plateServiceClient.getWells(plateId);
    logger.info(String.format("Number of wells for plate %s found: %s", plateId, wellDTOs.size()));
    List<WellData> wells = wellDTOs.stream()
        .map(w -> new WellData(w.getId(), w.getWellType(), w.getRow(), w.getColumn()))
        .collect(Collectors.toList());
    logger.info(
        String.format("Number of WellDTO objects mapped to WellData records: %s", wells.size()));
    PlateData plate = new PlateData(plateId, plateDTO.getBarcode(), wells);

    String getProtocolByIdWithFeatures = """
        {
          getProtocolById(protocolId: %d) {
            id
            name
            features {
              id
              name
            }
          }
        }
        """;
    ProtocolDTO protocolDTO = protocolServiceGraphQlClient.getProtocolById(
        resultSetDTO.getProtocolId(), getProtocolByIdWithFeatures);
    List<FeatureData> features = protocolDTO.getFeatures().stream()
        .map(f -> new FeatureData(f.getId(), f.getName())).collect(Collectors.toList());
    ProtocolData protocol = new ProtocolData(protocolDTO.getId(), protocolDTO.getName(), features);

    return new PlateResultSetData(plate, protocol);
  }

  @QueryMapping
  public List<ProtocolData> protocolsByPlateId(@Argument long plateId)
      throws ResultSetNotFoundException {
    List<ProtocolData> result = new ArrayList<>();

    List<ResultSetDTO> resultSets = resultSetService.getResultSetsByPlateId(plateId,
        Optional.empty());
    if (resultSets.isEmpty()) {
      return result;
    }

    List<Long> protocolIds = resultSets.stream().map(rs -> rs.getProtocolId()).distinct()
        .collect(Collectors.toList());
    if (protocolIds.isEmpty()) {
      return result;
    }

    String getProtocolByIdWithFeatures = """
        {
          getProtocolsByIds(protocolIds: [%s]) {
            id
            name
            features {
              id
              name
            }
          }
        }
        """;
    List<ProtocolDTO> protocolDTOs = protocolServiceGraphQlClient.getProtocolsByIds(protocolIds,
        getProtocolByIdWithFeatures);
    return protocolDTOs.stream().map(protocolDTO -> {
      return new ProtocolData(protocolDTO.getId(), protocolDTO.getName(),
          protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName()))
              .collect(Collectors.toList()));
    }).toList();
  }

  @QueryMapping
  public List<ProtocolData> protocolsByExperimentId(@Argument long experimentId)
      throws ResultSetNotFoundException {
    List<ProtocolData> result = new ArrayList<>();

    List<PlateDTO> plates = plateServiceClient.getPlatesByExperiment(experimentId);
    if (plates.isEmpty()) {
      return result;
    }

    Map<Long, PlateDTO> plateIdMap = plates.stream()
        .collect(Collectors.toMap(PlateDTO::getId, p -> p));
    List<ResultSetDTO> resultSets = resultSetService.getResultSetsByPlateIds(
        plateIdMap.keySet().stream().toList());
    if (resultSets.isEmpty()) {
      return result;
    }

    List<Long> protocolIds = resultSets.stream().map(rs -> rs.getProtocolId()).distinct()
        .collect(Collectors.toList());
    if (protocolIds.isEmpty()) {
      return result;
    }

    String getProtocolByIdWithFeatures = """
        {
          getProtocolsByIds(protocolIds: [%s]) {
            id
            name
            features {
              id
              name
            }
          }
        }
        """;
    List<ProtocolDTO> protocolDTOs = protocolServiceGraphQlClient.getProtocolsByIds(protocolIds,
        getProtocolByIdWithFeatures);
    return protocolDTOs.stream().map(protocolDTO -> {
      return new ProtocolData(protocolDTO.getId(), protocolDTO.getName(),
          protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName()))
              .toList());
    }).toList();
  }

  @QueryMapping
  public List<FeatureValue> featureValuesByPlateIdAndFeatureIdAndProtocolId(@Argument long plateId,
      @Argument long featureId, @Argument long protocolId)
      throws ResultSetNotFoundException, ResultDataNotFoundException, UnresolvableObjectException {
    List<ResultSetDTO> latestResultSets = resultSetService.getLatestResultSetsByPlateId(plateId,
        Optional.empty(), Optional.of(protocolId));
    ResultSetDTO latestResultSet = latestResultSets.get(0);

    ResultDataDTO resultData = resultDataService.getResultDataByResultSetIdAndFeatureId(
        latestResultSet.getId(), featureId);
    List<WellDTO> wells = plateServiceClient.getWells(plateId);

    return IntStream.range(0, resultData.getValues().length)
        .mapToObj(i -> new FeatureValue(plateId, featureId, resultData.getValues()[i],
            wells.get(i).getId(), wells.get(i).getWellType(), wells.get(i).getRow(),
            wells.get(i).getColumn()))
        .collect(Collectors.toList());
  }
}
