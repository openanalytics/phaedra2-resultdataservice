package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.config.KafkaConfig;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final ResultDataService resultDataService;
    private final ResultFeatureStatService resultFeatureStatService;

    public KafkaConsumerService(ResultDataService resultDataService, ResultFeatureStatService resultFeatureStatService) {
        this.resultDataService = resultDataService;
        this.resultFeatureStatService = resultFeatureStatService;
    }

    @KafkaListener(topics = KafkaConfig.RESULTDATA_TOPIC, groupId = "resultdata-service", filter = "saveResultDataEventFilter")
    public void onSaveResultDataEvent(ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
        resultDataService.create(resultDataDTO.getResultSetId(), resultDataDTO);
    }

    @KafkaListener(topics = KafkaConfig.SAVE_FEATURE_RESULTDATA_EVENT, groupId = "resultdata-service", filter = "saveResultStatsEventFilter")
    public void onSaveResultStatsEvent(ResultFeatureStatDTO featureStatDTO) throws ResultSetNotFoundException, DuplicateResultFeatureStatException, ResultSetAlreadyCompletedException {
        resultFeatureStatService.create(featureStatDTO.getResultSetId(), featureStatDTO);
    }
}
