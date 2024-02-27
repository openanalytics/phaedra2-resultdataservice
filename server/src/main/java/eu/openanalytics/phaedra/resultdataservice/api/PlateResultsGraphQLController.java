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

import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.PlateUnresolvableException;
import eu.openanalytics.phaedra.plateservice.dto.PlateDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.ProtocolDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.*;
import eu.openanalytics.phaedra.resultdataservice.service.ResultDataService;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PlateResultsGraphQLController {

    private final ResultSetService resultSetService;
    private final ResultDataService resultDataService;
    private final ProtocolServiceClient protocolServiceClient;
    private final PlateServiceClient plateServiceClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PlateResultsGraphQLController(ResultSetService resultSetService, ResultDataService resultDataService, ProtocolServiceClient protocolServiceClient, PlateServiceClient plateServiceClient) {
        this.resultSetService = resultSetService;
        this.resultDataService = resultDataService;
        this.protocolServiceClient = protocolServiceClient;
        this.plateServiceClient = plateServiceClient;
    }

    @QueryMapping
    public PlateResultSetData latestPlateResultByPlateId(@Argument long plateId) throws ResultSetNotFoundException, PlateUnresolvableException, ProtocolUnresolvableException {
        ResultSetDTO resultSetDTO = resultSetService.getLatestResultSetByPlateId(plateId, Optional.empty());

        PlateDTO plateDTO = plateServiceClient.getPlate(plateId);
        List<WellDTO> wellDTOs = plateServiceClient.getWells(plateId);
        logger.info(String.format("Number of wells for plate %s found: %s", plateId, wellDTOs.size()));
        List<WellData> wells = wellDTOs.stream().map(w -> new WellData(w.getId(), w.getWellType(), w.getRow(), w.getColumn())).collect(Collectors.toList());
        logger.info(String.format("Number of WellDTO objects mapped to WellData records: %s", wells.size()));
        PlateData plate = new PlateData(plateId, plateDTO.getBarcode(), wells);

        ProtocolDTO protocolDTO = protocolServiceClient.getProtocol(resultSetDTO.getProtocolId());
        List<FeatureData> features = protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName())).collect(Collectors.toList());
        ProtocolData protocol = new ProtocolData(protocolDTO.getId(), protocolDTO.getName(), features);

        return new PlateResultSetData(plate, protocol);
    }

    @QueryMapping
    public List<ProtocolData> protocolsByPlateId(@Argument long plateId) throws ResultSetNotFoundException {
        List<ProtocolData> result = new ArrayList<>();

        List<ResultSetDTO> resultSets = resultSetService.getResultSetsByPlateId(plateId, Optional.empty());
        if (resultSets.isEmpty()) return result;

        List<Long> protocolIds = resultSets.stream().map(rs -> rs.getProtocolId()).distinct().collect(Collectors.toList());
        return protocolIds.stream().map(pId -> {
            try {
                ProtocolDTO protocolDTO = protocolServiceClient.getProtocol(pId);
                List<FeatureData> features = protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName())).collect(Collectors.toList());
                return  new ProtocolData(protocolDTO.getId(), protocolDTO.getName(), features);
            } catch (ProtocolUnresolvableException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @QueryMapping
    public List<ProtocolData> protocolsByExperimentId(@Argument long experimentId) throws ResultSetNotFoundException {
        List<ProtocolData> result = new ArrayList<>();

        List<PlateDTO> plates = plateServiceClient.getPlatesByExperiment(experimentId);

        List<ResultSetDTO> resultSets = new ArrayList<>();
        for (PlateDTO plate: plates) {
            resultSets.addAll(resultSetService.getResultSetsByPlateId(plate.getId(), Optional.empty()));
        }

        if (resultSets.isEmpty()) return result;

        List<Long> protocolIds = resultSets.stream().map(rs -> rs.getProtocolId()).distinct().collect(Collectors.toList());
        return protocolIds.stream().map(pId -> {
            try {
                ProtocolDTO protocolDTO = protocolServiceClient.getProtocol(pId);
                List<FeatureData> features = protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName())).collect(Collectors.toList());
                return  new ProtocolData(protocolDTO.getId(), protocolDTO.getName(), features);
            } catch (ProtocolUnresolvableException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @QueryMapping
    public List<FeatureValue> featureValuesByPlateIdAndFeatureIdAndProtocolId(@Argument long plateId, @Argument long featureId, @Argument long protocolId) throws ResultSetNotFoundException, ResultDataNotFoundException, PlateUnresolvableException {
        List<ResultSetDTO> latestResultSets = resultSetService.getLatestResultSetsByPlateId(plateId, Optional.empty(), Optional.of(protocolId));
        ResultSetDTO latestResultSet = latestResultSets.get(0);

        ResultDataDTO resultData = resultDataService.getResultDataByResultSetIdAndFeatureId(latestResultSet.getId(), featureId);
        List<WellDTO> wells = plateServiceClient.getWells(plateId);

        return IntStream.range(0, resultData.getValues().length)
                .mapToObj(i -> new FeatureValue(plateId, featureId, resultData.getValues()[i], wells.get(i).getId(), wells.get(i).getWellType(), wells.get(i).getRow(), wells.get(i).getColumn()))
                .collect(Collectors.toList());
    }
}
