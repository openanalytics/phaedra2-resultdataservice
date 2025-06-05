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
package eu.openanalytics.phaedra.resultdataservice.client;


import eu.openanalytics.phaedra.resultdataservice.client.exception.CurveUnresolvedException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultFeatureStatUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.CurveDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;

import java.util.List;
import java.util.Optional;

public interface ResultDataServiceClient {

    ResultSetDTO createResultDataSet(long protocolId, long plateId, long measId) throws ResultSetUnresolvableException;

    ResultSetDTO completeResultDataSet(long resultSetId, StatusCode outcome, List<ErrorDTO> errors, String errorsText) throws ResultSetUnresolvableException;

    ResultDataDTO addResultData(long resultSetId, long featureId, float[] values, StatusCode statusCode, String statusMessage) throws ResultDataUnresolvableException;

    ResultDataDTO getResultData(long resultSetId, long featureId) throws ResultDataUnresolvableException;

    ResultFeatureStatDTO createResultFeatureStat(long resultSetId, long featureId, long featureStatId, Optional<Float> value, String statisticName, String welltype,
                                                 StatusCode statusCode, String statusMessage) throws ResultFeatureStatUnresolvableException;

    List<ResultFeatureStatDTO> createResultFeatureStats(long resultSetId, List<ResultFeatureStatDTO> resultFeatureStats) throws ResultFeatureStatUnresolvableException;

    ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultFeatureStatUnresolvableException;

    ResultSetDTO getResultSet(long resultSetId) throws ResultSetUnresolvableException;

    ResultSetDTO getLatestResultSetByPlateId(long plateId) throws ResultSetUnresolvableException;

    ResultSetDTO getLatestResultSetByPlateIdAndMeasId(long plateId, long measId) throws ResultSetUnresolvableException;

    ResultSetDTO getLatestResultSetByPlateIdAndProtocolId(long plateId, long protocolId) throws ResultSetUnresolvableException;

    List<ResultSetDTO> getResultSet(StatusCode outcome) throws ResultSetUnresolvableException;

    List<ResultDataDTO> getResultData(long resultSetId) throws ResultDataUnresolvableException;

    List<ResultFeatureStatDTO> getResultFeatureStat(long resultSetId) throws ResultFeatureStatUnresolvableException;

    List<ResultFeatureStatDTO> getResultFeatureStatByResultSetIdAndFeatureId(long resultSetId, long featureId) throws ResultFeatureStatUnresolvableException;

    List<ResultFeatureStatDTO> getLatestResultFeatureStatsForPlateId(long plateId) throws ResultFeatureStatUnresolvableException;

    List<ResultFeatureStatDTO> getLatestResultFeatureStatsForPlateIdAndFeatureId(long plateId, long featureId) throws ResultFeatureStatUnresolvableException;

    CurveDTO createNewCurve(String substanceName, Long plateId, Long protocolId, Long featureId, Long resultSetId) throws CurveUnresolvedException;

    CurveDTO createNewCurve(String substanceName, Long plateId, Long protocolId, Long featureId, Long resultSetId, float[] dose, float[] prediction) throws CurveUnresolvedException;
}
