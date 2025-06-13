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
package eu.openanalytics.phaedra.resultdataservice.service;

import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.CURVE_DATA_GROUP_ID;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.EVENT_SAVE_CURVE;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.RESULT_DATA_GROUP_ID;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.TOPIC_CURVEDATA;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.TOPIC_RESULTDATA;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.dto.CurveDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;

@Service
public class KafkaConsumerService {

  private static final String LOG_EVENT_RECEIVED = "Event received to save {} for resultSet {}, feature {}";
  private static final String LOG_CURVE_CREATED = "Event received to create curve for substance {} and featureId {}";

  private final ResultDataService resultDataService;
  private final FeatureStatService featureStatService;
  private final CurveService curveService;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public KafkaConsumerService(ResultDataService resultDataService,
      FeatureStatService featureStatService,
      CurveService curveService) {
    this.resultDataService = resultDataService;
    this.featureStatService = featureStatService;
    this.curveService = curveService;
  }

  @KafkaListener(topics = TOPIC_RESULTDATA, groupId = RESULT_DATA_GROUP_ID + "_resData",
      filter = "saveResultDataEventFilter")
  public void handleResultDataEvent(ResultDataDTO resultDataDTO)
      throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
    logResultDataEvent(resultDataDTO);
    resultDataService.create(resultDataDTO.getResultSetId(), resultDataDTO);
  }

  @KafkaListener(topics = TOPIC_RESULTDATA, groupId = RESULT_DATA_GROUP_ID + "_resStats",
      filter = "saveResultStatsEventFilter")
  public void handleResultStatsEvent(ResultFeatureStatDTO featureStatDTO)
      throws ResultSetNotFoundException, DuplicateResultFeatureStatException, ResultSetAlreadyCompletedException {
    logFeatureStatsEvent(featureStatDTO);
    featureStatService.create(featureStatDTO.getResultSetId(), featureStatDTO);
  }

  @KafkaListener(topics = TOPIC_CURVEDATA, groupId = CURVE_DATA_GROUP_ID, filter = "saveCurveEventFilter")
  public void handleCurveDataEvent(CurveDTO curveDTO) {
    logCurveEvent(curveDTO);
    curveService.createCurve(curveDTO);
  }

  private void logResultDataEvent(ResultDataDTO resultDataDTO) {
    logger.info(LOG_EVENT_RECEIVED, "resultData",
        resultDataDTO.getResultSetId(), resultDataDTO.getFeatureId());
  }

  private void logFeatureStatsEvent(ResultFeatureStatDTO featureStatDTO) {
    logger.info(LOG_EVENT_RECEIVED + ", stat {}", "featureStats",
        featureStatDTO.getResultSetId(), featureStatDTO.getFeatureId(),
        featureStatDTO.getStatisticName());
  }

  private void logCurveEvent(CurveDTO curveDTO) {
    logger.info(LOG_CURVE_CREATED, curveDTO.getSubstanceName(), curveDTO.getFeatureId());
  }
}
