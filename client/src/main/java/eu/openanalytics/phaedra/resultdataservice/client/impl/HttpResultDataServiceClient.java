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
package eu.openanalytics.phaedra.resultdataservice.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceClient;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultFeatureStatUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.util.PhaedraRestTemplate;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;

public class HttpResultDataServiceClient implements ResultDataServiceClient {

    private final PhaedraRestTemplate restTemplate;
    private final IAuthorizationService authService;
    private final UrlFactory urlFactory;

    private static final String PROP_BASE_URL = "phaedra.resultdata-service.base-url";
    private static final String DEFAULT_BASE_URL = "http://phaedra-resultdata-service:8080/phaedra/resultdata-service";

    private final static ParameterizedTypeReference<PageDTO<ResultSetDTO>> PAGED_RESULTSET_TYPE = new ParameterizedTypeReference<>() {};
    private final static ParameterizedTypeReference<PageDTO<ResultDataDTO>> PAGED_RESULTDATA_TYPE = new ParameterizedTypeReference<>() {};
    private final static ParameterizedTypeReference<PageDTO<ResultFeatureStatDTO>> PAGED_RESULT_FEATURE_STAT_TYPE = new ParameterizedTypeReference<>() {};

    public HttpResultDataServiceClient(PhaedraRestTemplate restTemplate, IAuthorizationService authService, Environment environment) {
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.urlFactory = new UrlFactory(environment.getProperty(PROP_BASE_URL, DEFAULT_BASE_URL));
    }

    @Override
    public ResultSetDTO createResultDataSet(long protocolId, long plateId, long measId) throws ResultSetUnresolvableException {
        var resultSet = ResultSetDTO.builder()
            .protocolId(protocolId)
            .plateId(plateId)
            .measId(measId)
            .build();

        HttpEntity<?> httpEntity = new HttpEntity<>(resultSet, makeHttpHeaders());
        try {
            var res = restTemplate.postForObject(urlFactory.resultSet(), httpEntity, ResultSetDTO.class);
            if (res == null) {
                throw new ResultSetUnresolvableException("ResultSet could not be converted");
            }
            return res;
        } catch (HttpClientErrorException ex) {
            throw new ResultSetUnresolvableException("Error while creating ResultSet", ex);
        } catch (HttpServerErrorException ex) {
            throw new ResultSetUnresolvableException("Server Error while creating ResultSet", ex);
        }
    }

    @Override
    public ResultSetDTO completeResultDataSet(long resultSetId, StatusCode outcome, List<ErrorDTO> errors, String errorsText) throws ResultSetUnresolvableException {
        Objects.requireNonNull(outcome, "Outcome may not be null");
        var resultSet = ResultSetDTO.builder()
            .outcome(outcome)
            .errors(errors)
            .errorsText(errorsText)
            .build();

        HttpEntity<?> httpEntity = new HttpEntity<>(resultSet, makeHttpHeaders());
        try {
            var res = restTemplate.putForObject(urlFactory.resultSet(resultSetId), httpEntity, ResultSetDTO.class);
            if (res == null) {
                throw new ResultSetUnresolvableException("ResultSet could not be converted");
            }
            return res;
        } catch (HttpClientErrorException ex) {
            throw new ResultSetUnresolvableException("Error while creating ResultSet", ex);
        }
    }

    @Override
    public ResultDataDTO addResultData(long resultSetId, long featureId, float[] values, StatusCode statusCode, String statusMessage) throws ResultDataUnresolvableException {
        Objects.requireNonNull(values, "Values may not be null");
        Objects.requireNonNull(statusCode, "StatusCode may not be null");
        Objects.requireNonNull(statusMessage, "StatusMessage may not be null");

        var resultData = ResultDataDTO.builder()
            .featureId(featureId)
            .values(values)
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .build();

        HttpEntity<?> httpEntity = new HttpEntity<>(resultData, makeHttpHeaders());
        try {
            var res = restTemplate.postForObject(urlFactory.resultData(resultSetId), httpEntity, ResultDataDTO.class);
            if (res == null) {
                throw new ResultDataUnresolvableException("ResultData could not be converted");
            }
            return res;
        } catch (HttpClientErrorException ex) {
            throw new ResultDataUnresolvableException("Error while creating ResultData", ex);
        }
    }

    @Override
    public ResultDataDTO getResultData(long resultSetId, long featureId) throws ResultDataUnresolvableException {
        try {
            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            var resultData = restTemplate.exchange(urlFactory.resultDataByFeatureId(resultSetId, featureId), HttpMethod.GET, httpEntity, PAGED_RESULTDATA_TYPE);

            if (resultData.getStatusCode().isError()) {
                throw new ResultDataUnresolvableException("ResultData could not be converted");
            }
            if (resultData.getBody().getStatus().getTotalElements() == 0) {
                throw new ResultDataUnresolvableException("ResultData did not contain any data");
            }
            if (resultData.getBody().getStatus().getTotalElements() > 1) {
                throw new ResultDataUnresolvableException("ResultData did contain too many data");
            }
            return resultData.getBody().getData().get(0);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultDataUnresolvableException("ResultData not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultDataUnresolvableException("Error while fetching ResultData");
        }
    }

    @Override
    public ResultFeatureStatDTO createResultFeatureStat(long resultSetId, long featureId, long featureStatId,
                                                        Optional<Float> value, String statisticName, String welltype,
                                                        StatusCode statusCode, String statusMessage) throws ResultFeatureStatUnresolvableException {
        return createResultFeatureStats(resultSetId,
            List.of(
                ResultFeatureStatDTO.builder()
                    .featureId(featureId)
                    .featureStatId(featureStatId)
                    .value(value.orElse(null))
                    .statisticName(statisticName)
                    .welltype(welltype)
                    .statusCode(statusCode)
                    .statusMessage(statusMessage)
                    .build())
        	).get(0);
    }

    @Override
    public List<ResultFeatureStatDTO> createResultFeatureStats(long resultSetId, List<ResultFeatureStatDTO> resultFeatureStats) throws ResultFeatureStatUnresolvableException {
        try {
            HttpEntity<?> httpEntity = new HttpEntity<>(resultFeatureStats, makeHttpHeaders());
            var res = restTemplate.postForObject(urlFactory.resultFeatureStat(resultSetId), httpEntity, ResultFeatureStatDTO[].class);
            if (res == null) {
                throw new ResultFeatureStatUnresolvableException("ResultFeatureStat could not be converted");
            }
            return Arrays.asList(res);
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while creating ResultFeatureStat", ex);
        } catch (HttpServerErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Server Error while creating ResultFeatureStat", ex);
        }
    }

    @Override
    public ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultFeatureStatUnresolvableException {
        try {
            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            var resultFeatureStat = restTemplate.exchange(urlFactory.resultFeatureStatByFeatureStatId(resultSetId, resultFeatureStatId), HttpMethod.GET, httpEntity, ResultFeatureStatDTO.class);
            if (resultFeatureStat.getStatusCode().isError()) {
                throw new ResultFeatureStatUnresolvableException("ResultFeatureStat could not be converted");
            }
            return resultFeatureStat.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultFeatureStatUnresolvableException("ResultFeatureStat not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while fetching ResultFeatureStat");
        }
    }

    @Override
    public ResultSetDTO getResultSet(long resultSetId) throws ResultSetUnresolvableException {
        try {
            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            var resultSet = restTemplate.exchange(urlFactory.resultSet(resultSetId), HttpMethod.GET, httpEntity, ResultSetDTO.class);
            if (resultSet.getStatusCode().isError()) {
                throw new ResultSetUnresolvableException("Error while fetching ResultSet");
            }
            return resultSet.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultSetUnresolvableException("ResultSet not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultSetUnresolvableException("Error while fetching ResultSet");
        }
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateId(long plateId) throws ResultSetUnresolvableException {
        var resultSet = restTemplate.exchange(urlFactory.latestResultSetByPlateId(plateId), HttpMethod.GET, new HttpEntity<>(makeHttpHeaders()), ResultSetDTO[].class, plateId);

        if (resultSet.getStatusCode().isError()) {
            throw new ResultSetUnresolvableException("ResultSet could not be converted");
        }

        return ArrayUtils.isNotEmpty(resultSet.getBody()) ? resultSet.getBody()[0] : null;
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateIdAndMeasId(long plateId, long measId) throws ResultSetUnresolvableException {
        HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
        var resultSet = restTemplate.exchange(urlFactory.latestResultSetByPlateIdAndMeasId(plateId, measId), HttpMethod.GET, httpEntity, ResultSetDTO[].class, plateId, measId);

        if (resultSet.getStatusCode().isError()) {
            throw new ResultSetUnresolvableException("ResultSet could not be converted");
        }

        return ArrayUtils.isNotEmpty(resultSet.getBody()) ? resultSet.getBody()[0] : null;
    }

    @Override
    public ResultSetDTO getLatestResultSetByPlateIdAndProtocolId(long plateId, long protocolId) throws ResultSetUnresolvableException {
        HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
        var resultSet = restTemplate.exchange(urlFactory.latestResultSetByPlateIdAndProtocolId(plateId, protocolId), HttpMethod.GET, httpEntity, ResultSetDTO[].class, plateId, protocolId);

        if (resultSet.getStatusCode().isError()) {
            throw new ResultSetUnresolvableException("ResultSet could not be converted");
        }

        return ArrayUtils.isNotEmpty(resultSet.getBody()) ? resultSet.getBody()[0] : null;
    }

    @Override
    public List<ResultSetDTO> getResultSet(StatusCode outcome) throws ResultSetUnresolvableException {
        try {
            var currentPage = 0;
            var hasNextPage = true;
            var result = new ArrayList<ResultSetDTO>();

            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            do {
                var resultSet = restTemplate.exchange(urlFactory.resultSet(outcome, currentPage), HttpMethod.GET, httpEntity, PAGED_RESULTSET_TYPE);

                if (resultSet.getStatusCode().isError()) {
                    throw new ResultSetUnresolvableException("ResultSet could not be converted");
                }

                result.addAll(resultSet.getBody().getData());

                hasNextPage = !resultSet.getBody().getStatus().isLast();
                currentPage++;
            } while (hasNextPage);
            return result;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultSetUnresolvableException("ResultSet not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultSetUnresolvableException("Error while fetching ResultSet");
        }
    }

    @Override
    public List<ResultDataDTO> getResultData(long resultSetId) throws ResultDataUnresolvableException {
        try {
            var currentPage = 0;
            var hasNextPage = true;
            var result = new ArrayList<ResultDataDTO>();

            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            do {
                var resultData = restTemplate.exchange(urlFactory.resultData(resultSetId, currentPage), HttpMethod.GET, httpEntity, PAGED_RESULTDATA_TYPE);

                if (resultData.getStatusCode().isError()) {
                    throw new ResultDataUnresolvableException("ResultData could not be converted");
                }

                result.addAll(resultData.getBody().getData());

                hasNextPage = !resultData.getBody().getStatus().isLast();
                currentPage++;
            } while (hasNextPage);
            return result;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultDataUnresolvableException("ResultData not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultDataUnresolvableException("Error while fetching ResultData");
        }
    }

    @Override
    public List<ResultFeatureStatDTO> getResultFeatureStat(long resultSetId) throws ResultFeatureStatUnresolvableException {
        try {
            var currentPage = 0;
            var hasNextPage = true;
            var result = new ArrayList<ResultFeatureStatDTO>();

            HttpEntity<?> httpEntity = new HttpEntity<>(makeHttpHeaders());
            do {
                var resultFeatures = restTemplate.exchange(urlFactory.resultFeatureStat(resultSetId, currentPage), HttpMethod.GET, httpEntity, PAGED_RESULT_FEATURE_STAT_TYPE);

                if (resultFeatures.getStatusCode().isError()) {
                    throw new ResultFeatureStatUnresolvableException("ResultFeatureStat could not be converted");
                }

                result.addAll(resultFeatures.getBody().getData());

                hasNextPage = !resultFeatures.getBody().getStatus().isLast();
                currentPage++;
            } while (hasNextPage);
            return result;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultFeatureStatUnresolvableException("ResultFeatureStat not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while fetching ResultFeatureStat");
        }
    }

    @Override
    public List<ResultFeatureStatDTO> getResultFeatureStatByResultSetIdAndFeatureId(long resultSetId, long featureId) throws ResultFeatureStatUnresolvableException {
        List<ResultFeatureStatDTO> result = getResultFeatureStat(resultSetId);
        return result.stream().filter(rfs -> rfs.getFeatureId() == featureId).toList();
    }

    @Override
    public List<ResultFeatureStatDTO> getLatestResultFeatureStatsForPlateId(long plateId)
        throws ResultFeatureStatUnresolvableException {
        List<ResultFeatureStatDTO> result = new ArrayList<>();
      try {
        ResultSetDTO resultSetDTO = getLatestResultSetByPlateId(plateId);
        if (resultSetDTO != null)
            result.addAll(getResultFeatureStat(resultSetDTO.getId()));
      } catch (ResultSetUnresolvableException e) {
        throw new ResultFeatureStatUnresolvableException(e.getMessage());
      }

      return result;
    }

    @Override
    public List<ResultFeatureStatDTO> getLatestResultFeatureStatsForPlateIdAndFeatureId(long plateId, long featureId) throws ResultFeatureStatUnresolvableException {
        List<ResultFeatureStatDTO> result = new ArrayList<>();
        try {
            ResultSetDTO resultSetDTO = getLatestResultSetByPlateId(plateId);
            if (resultSetDTO != null)
                result.addAll(getResultFeatureStatByResultSetIdAndFeatureId(resultSetDTO.getId(), featureId));
        } catch (ResultSetUnresolvableException e) {
            throw new ResultFeatureStatUnresolvableException(e.getMessage());
        }

        return result;
    }

    private HttpHeaders makeHttpHeaders() {
    	HttpHeaders httpHeaders = new HttpHeaders();
        String bearerToken = authService.getCurrentBearerToken();
    	if (bearerToken != null) httpHeaders.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", bearerToken));
    	return httpHeaders;
    }
}
