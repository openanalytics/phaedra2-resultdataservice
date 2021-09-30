package eu.openanalytics.phaedra.resultdataservice.client.impl;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HttpResultDataServiceClient implements ResultDataServiceClient {

    private final PhaedraRestTemplate restTemplate;

    private final static ParameterizedTypeReference<PageDTO<ResultDataDTO>> PAGED_RESULTDATA_TYPE = new ParameterizedTypeReference<>() {
    };

    private final static ParameterizedTypeReference<PageDTO<ResultFeatureStatDTO>> PAGED_RESULT_FEATURE_STAT_TYPE = new ParameterizedTypeReference<>() {
    };

    public HttpResultDataServiceClient(PhaedraRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResultSetDTO createResultDataSet(long protocolId, long plateId, long measId) throws ResultSetUnresolvableException {
        var resultSet = ResultSetDTO.builder()
            .protocolId(protocolId)
            .plateId(plateId)
            .measId(measId)
            .build();

        try {
            var res = restTemplate.postForObject(UrlFactory.resultSet(), resultSet, ResultSetDTO.class);
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

        try {
            var res = restTemplate.putForObject(UrlFactory.resultSet(resultSetId), resultSet, ResultSetDTO.class);
            if (res == null) {
                throw new ResultSetUnresolvableException("ResultSet could not be converted");
            }
            return res;
        } catch (HttpClientErrorException ex) {
            throw new ResultSetUnresolvableException("Error while creating ResultSet", ex);
        }
    }

    @Override
    public ResultDataDTO addResultData(long resultSetId, long featureId, float[] values, StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultDataUnresolvableException {
        Objects.requireNonNull(values, "Values may not be null");
        Objects.requireNonNull(statusCode, "StatusCode may not be null");
        Objects.requireNonNull(statusMessage, "StatusMessage may not be null");
        Objects.requireNonNull(exitCode, "ExitCode may not be null");

        var resultData = ResultDataDTO.builder()
            .featureId(featureId)
            .values(values)
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .exitCode(exitCode)
            .build();

        try {
            var res = restTemplate.postForObject(UrlFactory.resultData(resultSetId), resultData, ResultDataDTO.class);
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
            var resultData = restTemplate.getForObject(UrlFactory.resultDataByFeatureId(resultSetId, featureId), PAGED_RESULTDATA_TYPE);

            if (resultData == null) {
                throw new ResultDataUnresolvableException("ResultData could not be converted");
            }
            if (resultData.getStatus().getTotalElements() == 0) {
                throw new ResultDataUnresolvableException("ResultData did not contain any data");
            }
            if (resultData.getStatus().getTotalElements() > 1) {
                throw new ResultDataUnresolvableException("ResultData did contain too many data");
            }
            return resultData.getData().get(0);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultDataUnresolvableException("ResultData not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultDataUnresolvableException("Error while fetching ResultData");
        }
    }

    @Override
    public ResultFeatureStatDTO createResultFeatureStat(long resultSetId, long featureId, long featureStatId, Optional<Float> value, String statisticName, String welltype,
                                                        StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultFeatureStatUnresolvableException {
        var resultFeatureStat = ResultFeatureStatDTO.builder()
            .featureId(featureId)
            .featureStatId(featureStatId)
            .value(value.orElse(null))
            .statisticName(statisticName)
            .welltype(welltype)
            .statusCode(statusCode)
            .statusMessage(statusMessage)
            .exitCode(exitCode)
            .build();
        try {
            var res = restTemplate.postForObject(UrlFactory.resultFeatureStat(resultSetId), resultFeatureStat, ResultFeatureStatDTO.class);
            if (res == null) {
                throw new ResultFeatureStatUnresolvableException("ResultFeatureStat could not be converted");
            }
            return res;
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while creating ResultFeatureStat", ex);
        } catch (HttpServerErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Server Error while creating ResultFeatureStat", ex);
        }
    }

    @Override
    public ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultFeatureStatUnresolvableException {
        try {
            var resultFeatureStat = restTemplate.getForObject(UrlFactory.resultFeatureStatByFeatureStatId(resultSetId, resultFeatureStatId), ResultFeatureStatDTO.class);

            if (resultFeatureStat == null) {
                throw new ResultFeatureStatUnresolvableException("ResultFeatureStat could not be converted");
            }
            return resultFeatureStat;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultFeatureStatUnresolvableException("ResultFeatureStat not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while fetching ResultFeatureStat");
        }
    }

    @Override
    public ResultSetDTO getResultSet(long resultSetId) throws ResultSetUnresolvableException {
        try {
            var resultSet = restTemplate.getForObject(UrlFactory.resultSet(resultSetId), ResultSetDTO.class);

            return resultSet;
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
            do {
                var resultData = restTemplate.getForObject(UrlFactory.resultData(resultSetId, currentPage), PAGED_RESULTDATA_TYPE);

                if (resultData == null || resultData.getStatus() == null) {
                    throw new ResultDataUnresolvableException("ResultSet could not be converted");
                }

                result.addAll(resultData.getData());

                hasNextPage = !resultData.getStatus().isLast();
                currentPage++;
            } while (hasNextPage);
            return result;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultDataUnresolvableException("ResultSet not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultDataUnresolvableException("Error while fetching ResultSet");
        }
    }

    @Override
    public List<ResultFeatureStatDTO> getResultFeatureStat(long resultSetId) throws ResultFeatureStatUnresolvableException {
        try {
            var currentPage = 0;
            var hasNextPage = true;
            var result = new ArrayList<ResultFeatureStatDTO>();
            do {
                var resultFeatures = restTemplate.getForObject(UrlFactory.resultFeatureStat(resultSetId, currentPage), PAGED_RESULT_FEATURE_STAT_TYPE);

                if (resultFeatures == null || resultFeatures.getStatus() == null) {
                    throw new ResultFeatureStatUnresolvableException("ResultSet could not be converted");
                }

                result.addAll(resultFeatures.getData());

                hasNextPage = !resultFeatures.getStatus().isLast();
                currentPage++;
            } while (hasNextPage);
            return result;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResultFeatureStatUnresolvableException("ResultSet not found");
        } catch (HttpClientErrorException ex) {
            throw new ResultFeatureStatUnresolvableException("Error while fetching ResultSet");
        }
    }

}
