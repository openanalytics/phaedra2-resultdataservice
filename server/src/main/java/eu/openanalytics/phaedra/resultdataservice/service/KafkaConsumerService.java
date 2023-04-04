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
    private final ResultFeatureStatService resultFeatureStatService;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public KafkaConsumerService(ResultDataService resultDataService, ResultFeatureStatService resultFeatureStatService) {
        this.resultDataService = resultDataService;
        this.resultFeatureStatService = resultFeatureStatService;
    }

    @KafkaListener(topics = TOPIC_RESULTDATA, groupId = GROUP_ID, filter = "saveResultDataEventFilter")
    public void onSaveResultDataEvent(ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
    	logger.info("Event received to save resultData: " + resultDataDTO);
        resultDataService.create(resultDataDTO.getResultSetId(), resultDataDTO);
    }

    @KafkaListener(topics = TOPIC_RESULTDATA, groupId = GROUP_ID, filter = "saveResultStatsEventFilter")
    public void onSaveResultStatsEvent(ResultFeatureStatDTO featureStatDTO) throws ResultSetNotFoundException, DuplicateResultFeatureStatException, ResultSetAlreadyCompletedException {
    	logger.info("Event received to save featureStats: " + featureStatDTO);
        resultFeatureStatService.create(featureStatDTO.getResultSetId(), featureStatDTO);
    }
}
