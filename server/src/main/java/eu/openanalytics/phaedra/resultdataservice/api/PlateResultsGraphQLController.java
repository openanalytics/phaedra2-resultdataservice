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

import eu.openanalytics.phaedra.plateservice.client.PlateServiceClient;
import eu.openanalytics.phaedra.plateservice.client.exception.PlateUnresolvableException;
import eu.openanalytics.phaedra.plateservice.dto.PlateDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.protocolservice.client.ProtocolServiceClient;
import eu.openanalytics.phaedra.protocolservice.client.exception.ProtocolUnresolvableException;
import eu.openanalytics.phaedra.protocolservice.dto.ProtocolDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.*;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PlateResultsGraphQLController {

    private final ResultSetService resultSetService;
    private final ProtocolServiceClient protocolServiceClient;
    private final PlateServiceClient plateServiceClient;

    public PlateResultsGraphQLController(ResultSetService resultSetService, ProtocolServiceClient protocolServiceClient, PlateServiceClient plateServiceClient) {
        this.resultSetService = resultSetService;
        this.protocolServiceClient = protocolServiceClient;
        this.plateServiceClient = plateServiceClient;
    }

    @QueryMapping
    public PlateResultSetData latestPlateResultByPlateId(@Argument long plateId) throws ResultSetNotFoundException, PlateUnresolvableException, ProtocolUnresolvableException {
        ResultSetDTO resultSetDTO = resultSetService.getLatestResultSetByPlateId(plateId, null);
        PlateDTO plateDTO = plateServiceClient.getPlate(plateId);
        List<WellDTO> wellDTOs = plateServiceClient.getWells(plateId);
        ProtocolDTO protocolDTO = protocolServiceClient.getProtocol(resultSetDTO.getProtocolId());


        return new PlateResultSetData(new PlateData(plateDTO.getId(),
                wellDTOs.stream().map(w -> new WellData(w.getId(), w.getWellType(), w.getRow(), w.getColumn())).collect(Collectors.toList())),
                new ProtocolData(protocolDTO.getId(), protocolDTO.getName(), protocolDTO.getFeatures().stream().map(f -> new FeatureData(f.getId(), f.getName())).collect(Collectors.toList())));
    }
}
