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
package eu.openanalytics.phaedra.resultdataservice.service;

import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.GROUP_ID;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.TOPIC_RESULTDATA;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;

@Service
public class KafkaConsumerService {

    private final ResultDataService resultDataService;
    private final FeatureStatService featureStatService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public KafkaConsumerService(ResultDataService resultDataService, FeatureStatService featureStatService) {
        this.resultDataService = resultDataService;
        this.featureStatService = featureStatService;
    }

    @KafkaListener(topics = TOPIC_RESULTDATA, groupId = GROUP_ID + "_resData", filter = "saveResultDataEventFilter")
    public void onSaveResultDataEvent(ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
    	logger.info(String.format("Event received to save resultData for resultSet %d, feature %d", resultDataDTO.getResultSetId(), resultDataDTO.getFeatureId()));
        resultDataService.create(resultDataDTO.getResultSetId(), resultDataDTO);
    }

    @KafkaListener(topics = TOPIC_RESULTDATA, groupId = GROUP_ID + "_resStats", filter = "saveResultStatsEventFilter")
    public void onSaveResultStatsEvent(ResultFeatureStatDTO featureStatDTO) throws ResultSetNotFoundException, DuplicateResultFeatureStatException, ResultSetAlreadyCompletedException {
    	logger.info(String.format("Event received to save featureStats for resultSet %d, feature %d, stat %s", featureStatDTO.getResultSetId(), featureStatDTO.getFeatureId(), featureStatDTO.getStatisticName()));
        featureStatService.create(featureStatDTO.getResultSetId(), featureStatDTO);
    }
}
