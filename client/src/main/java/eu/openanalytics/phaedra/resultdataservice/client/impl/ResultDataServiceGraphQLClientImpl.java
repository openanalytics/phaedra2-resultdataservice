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
package eu.openanalytics.phaedra.resultdataservice.client.impl;

import eu.openanalytics.phaedra.resultdataservice.client.ResultDataServiceGraphQLClient;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import eu.openanalytics.phaedra.util.auth.IAuthorizationService;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ResultDataServiceGraphQLClientImpl implements ResultDataServiceGraphQLClient {

  private final IAuthorizationService authService;
  private final WebClient webClient;
  private static final String PROP_BASE_URL = "phaedra.resultdata-service.base-url";
  private static final String DEFAULT_BASE_URL = "http://phaedra-resultdata-service:8080/phaedra/resultdata-service";
  private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024;

  public ResultDataServiceGraphQLClientImpl(IAuthorizationService authService,
      Environment environment) {
    String baseUrl = environment.getProperty(PROP_BASE_URL, DEFAULT_BASE_URL);
    this.authService = authService;
    this.webClient = WebClient.builder()
        .baseUrl(String.format("%s/graphql", baseUrl))
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
        .build();
  }

  @Override
  public ResultSetDTO getResultSet(long resultSetId) {
    String document = """
          {
            resultSetById(resultSetId: %d) {
              %s
            }
          }
        """.formatted(resultSetId, buildGraphQLDocumentBody());
    return httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetById")
        .toEntity(ResultSetDTO.class);
  }

  @Override
  public List<ResultSetDTO> getResultSets(ResultSetFilter filter) {
    String document = """
          query($filter: ResultSetFilter) {
            resultSets(filter: $filter)  {
              %s
            }
          }
        """.formatted(buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .variable("filter", filter)
        .retrieveSync("resultSets")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByPlateId(Long plateId) {
    String document = """
          {
            resultSetsByPlateId(plateId: %d) {
              %s
            }
          }
        """.formatted(plateId, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByPlateId")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByPlateIds(List<Long> plateIds) {
    String document = """
          {
            resultSetsByPlateIds(plateIds: %s) {
              %s
            }
          }
        """.formatted(plateIds, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByPlateIds")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByMeasurementId(Long measurementId) {
    String document = """
          {
            resultSetsByMeasurementId(measurementId: %d) {
              %s
            }
          }
        """.formatted(measurementId, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByMeasurementId")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByMeasurementIds(List<Long> measurementIds) {
    String document = """
          {
            resultSetsByMeasurementIds(measurementIds: %s) {
              %s
            }
          }
        """.formatted(measurementIds, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByMeasurementIds")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByProtocolId(Long protocolId) {
    String document = """
          {
            resultSetsByProtocolId(protocolId: %d) {
              %s
            }
          }
        """.formatted(protocolId, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByProtocolId")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByProtocolIds(List<Long> protocolIds) {
    String document = """
          {
            resultSetsByProtocolIds(protocolIds: %s) {
              %s
            }
          }
        """.formatted(protocolIds, buildGraphQLDocumentBody());
    ResultSetDTO[] results = httpGraphQlClient()
        .document(document)
        .retrieveSync("resultSetsByProtocolIds")
        .toEntity(ResultSetDTO[].class);
    return List.of(results);
  }

  @Override
  public List<ResultSetDTO> getResultSetsByStatus(StatusCode status) {
    //TODO:
    return List.of();
  }

  @Override
  public List<ResultSetDTO> getResultSetsByStatus(List<StatusCode> statusList) {
    //TODO:
    return List.of();
  }

  private String buildGraphQLDocumentBody() {
    return """
          id
          protocolId
          plateId
          measId
          executionStartTimeStamp
          executionEndTimeStamp
          outcome
          errors {
            timestamp
            exceptionClassName
            exceptionMessage
            description
            featureId
            featureName
            sequenceNumber
            formulaId
            formulaName
            civType
            civVariableName
            civSource
            exitCode
            statusMessage
            featureStatId
            featureStatName
            newResultSetId
          }
          errorsText
        """;
  }

  private HttpGraphQlClient httpGraphQlClient() {
    String bearerToken = authService.getCurrentBearerToken();
    return HttpGraphQlClient.builder(this.webClient)
        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", bearerToken))
        .build();
  }

}
