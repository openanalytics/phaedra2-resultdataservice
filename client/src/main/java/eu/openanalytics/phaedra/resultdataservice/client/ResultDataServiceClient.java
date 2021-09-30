package eu.openanalytics.phaedra.resultdataservice.client;


import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultDataUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultFeatureStatUnresolvableException;
import eu.openanalytics.phaedra.resultdataservice.client.exception.ResultSetUnresolvableException;
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

    ResultDataDTO addResultData(long resultSetId, long featureId, float[] values, StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultDataUnresolvableException;

    ResultDataDTO getResultData(long resultSetId, long featureId) throws ResultDataUnresolvableException;

    ResultFeatureStatDTO createResultFeatureStat(long resultSetId, long featureId, long featureStatId, Optional<Float> value, String statisticName, String welltype,
                                                 StatusCode statusCode, String statusMessage, Integer exitCode) throws ResultFeatureStatUnresolvableException;

    ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultFeatureStatUnresolvableException;

    ResultSetDTO getResultSet(long resultSetId) throws ResultSetUnresolvableException;

    List<ResultDataDTO> getResultData(long resultSetId) throws ResultDataUnresolvableException;

    List<ResultFeatureStatDTO> getResultFeatureStat(long resultSetId) throws ResultFeatureStatUnresolvableException;
}
