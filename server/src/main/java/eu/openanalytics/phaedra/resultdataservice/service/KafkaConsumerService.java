package eu.openanalytics.phaedra.resultdataservice.service;

import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.EVENT_SAVE_RESULT_DATA;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.GROUP_ID;
import static eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig.TOPIC_RESULTDATA;

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

    public KafkaConsumerService(ResultDataService resultDataService, ResultFeatureStatService resultFeatureStatService) {
        this.resultDataService = resultDataService;
        this.resultFeatureStatService = resultFeatureStatService;
    }

    @KafkaListener(topics = TOPIC_RESULTDATA, groupId = GROUP_ID, filter = "saveResultDataEventFilter")
    public void onSaveResultDataEvent(ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
        resultDataService.create(resultDataDTO.getResultSetId(), resultDataDTO);
    }

    @KafkaListener(topics = EVENT_SAVE_RESULT_DATA, groupId = GROUP_ID, filter = "saveResultStatsEventFilter")
    public void onSaveResultStatsEvent(ResultFeatureStatDTO featureStatDTO) throws ResultSetNotFoundException, DuplicateResultFeatureStatException, ResultSetAlreadyCompletedException {
        resultFeatureStatService.create(featureStatDTO.getResultSetId(), featureStatDTO);
    }
}