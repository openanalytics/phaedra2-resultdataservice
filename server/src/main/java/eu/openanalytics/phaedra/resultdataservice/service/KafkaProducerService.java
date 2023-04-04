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
package eu.openanalytics.phaedra.resultdataservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;

@Service
public class KafkaProducerService {
	
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResultSetUpdated(ResultSetDTO resultSet) {
    	kafkaTemplate.send(KafkaConfig.TOPIC_RESULTDATA, KafkaConfig.EVENT_RESULT_SET_UPDATED, resultSet);
    	logger.info("Event sent for resultSet: " + resultSet);
    }
    
    public void sendResultDataUpdated(ResultDataDTO resultData) {
    	kafkaTemplate.send(KafkaConfig.TOPIC_RESULTDATA, KafkaConfig.EVENT_RESULT_DATA_UPDATED, resultData);
    	logger.info("Event sent for resultData: " + resultData);
    }
    
    public void sendResultFeatureStatUpdated(ResultFeatureStatDTO featureStat) {
    	kafkaTemplate.send(KafkaConfig.TOPIC_RESULTDATA, KafkaConfig.EVENT_RESULT_FEATURE_STAT_UPDATED, featureStat);
    	logger.info("Event sent for featureStats: " + featureStat);
    }
    
}
