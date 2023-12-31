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
package eu.openanalytics.phaedra.resultdataservice.client.impl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultFeatureStatUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;

public class CachingHttpResultDataServiceClient implements ResultDataServiceClient {

    private final HttpResultDataServiceClient httpResultDataServiceClient;

    private record ResultDataKey(long resultSetId, long featureId) {};

    private final Cache<ResultDataKey, ResultDataDTO> resultDataCache;
    private final Cache<Long, ResultSetDTO> resultSetCache;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CachingHttpResultDataServiceClient(PhaedraRestTemplate restTemplate, IAuthorizationService authService) {
        httpResultDataServiceClient = new HttpResultDataServiceClient(restTemplate, authService);
        resultDataCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();
        resultSetCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();
    }

    // CACHES

    @Override
    public ResultSetDTO completeResultDataSet(long resultSetId, StatusCode outcome, List<ErrorDTO> errors, String errorsText) throws ResultSetUnresolvableException {
        var res = httpResultDataServiceClient.completeResultDataSet(resultSetId, outcome, errors, errorsText);
        // ResultSet is immutable if the outcome is different from SCHEDULED
        if (res.getOutcome() != StatusCode.SCHEDULED) {
            resultSetCache.put(res.getId(), res);
        }
        return res;
    }

    @Override
    public ResultDataDTO addResultData(long resultSetId, long featureId, float[] values, StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultDataUnresolvableException {
        // ResultData is immutable -> cache it (note that it can be deleted)
        var resultData = httpResultDataServiceClient.addResultData(resultSetId, featureId, values, statusCode, statusMessage, exitCode);
        resultDataCache.put(new ResultDataKey(resultSetId, featureId), resultData);
        return resultData;
    }

    @Override
    public ResultDataDTO getResultData(long resultSetId, long featureId) throws ResultDataUnresolvableException {
        var key = new ResultDataKey(resultSetId, featureId);
        var resultData = resultDataCache.getIfPresent(key);
        if (resultData == null) {
            resultData = httpResultDataServiceClient.getResultData(resultSetId, featureId);
            resultDataCache.put(key, resultData);
        } else {
            logger.info(String.format("Retrieved object from cache: ResultData resultSetId=%s, featureId=%s",  resultSetId, featureId));
        }

        return resultData;
    }

    @Override
    public ResultSetDTO getResultSet(long resultSetId) throws ResultSetUnresolvableException {
        var resultSet = resultSetCache.getIfPresent(resultSetId);
        if (resultSet == null) {
            resultSet = httpResultDataServiceClient.getResultSet(resultSetId);
            if (resultSet.getOutcome() != StatusCode.SCHEDULED) {
                // ResultSet is only immutable if the outcome is different from SCHEDULED
                resultSetCache.put(resultSetId, resultSet);
            }
        } else {
            logger.info(String.format("Retrieved object from cache: ResultSet resultSetId=%s",  resultSetId));
        }

        return resultSet;
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateId(long plateId) throws ResultSetUnresolvableException {
        return httpResultDataServiceClient.getLatestResultSetByPlateId(plateId);
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateIdAndMeasId(long plateId, long measId) throws ResultSetUnresolvableException {
        return httpResultDataServiceClient.getLatestResultSetByPlateIdAndMeasId(plateId, measId);
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateIdAndProtocolId(long plateId, long protocolId) throws ResultSetUnresolvableException {
        return httpResultDataServiceClient.getLatestResultSetByPlateIdAndProtocolId(plateId, protocolId);
    }

    @Override
    public List<ResultSetDTO> getResultSet(StatusCode outcome) throws ResultSetUnresolvableException {
        return httpResultDataServiceClient.getResultSet(outcome);
    }

    // DELEGATES

    @Override
    public ResultSetDTO createResultDataSet(long protocolId, long plateId, long measId) throws ResultSetUnresolvableException {
        return httpResultDataServiceClient.createResultDataSet(protocolId, plateId, measId);
    }

    @Override
    public List<ResultDataDTO> getResultData(long resultSetId) throws ResultDataUnresolvableException {
        // we can not return these values from cache since values may be removed/added
        return httpResultDataServiceClient.getResultData(resultSetId);
    }
    @Override
    public ResultFeatureStatDTO createResultFeatureStat(long resultSetId, long featureId, long featureStatId,
                                                        Optional<Float> value, String statisticName, String welltype,
                                                        StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultFeatureStatUnresolvableException {
        return httpResultDataServiceClient.createResultFeatureStat(resultSetId, featureId, featureStatId,value, statisticName, welltype, statusCode, statusMessage, exitCode);
    }

    @Override
    public List<ResultFeatureStatDTO> createResultFeatureStats(long resultSetId, List<ResultFeatureStatDTO> resultFeatureStats) throws ResultFeatureStatUnresolvableException {
        return httpResultDataServiceClient.createResultFeatureStats(resultSetId, resultFeatureStats);
    }

    @Override
    public ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultFeatureStatUnresolvableException {
        return httpResultDataServiceClient.getResultFeatureStat(resultSetId, resultFeatureStatId);
    }

    @Override
    public List<ResultFeatureStatDTO> getResultFeatureStat(long resultSetId) throws ResultFeatureStatUnresolvableException {
        return httpResultDataServiceClient.getResultFeatureStat(resultSetId);
    }

}
