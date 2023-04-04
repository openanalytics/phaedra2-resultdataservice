package eu.openanalytics.phaedra.resultdataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Configuration
@EnableKafka
public class KafkaConfig {
	
	public static final String GROUP_ID = "resultdata-service";
	
    // Topics
    public static final String TOPIC_RESULTDATA = "resultdata";

    // Events
    public static final String EVENT_SAVE_RESULT_DATA = "saveResultData";
    public static final String EVENT_SAVE_RESULT_STATS = "saveResultStats";
    
    public static final String EVENT_RESULT_SET_UPDATED = "resultSetUpdated";
    public static final String EVENT_RESULT_DATA_UPDATED = "resultDataUpdated";
    public static final String EVENT_RESULT_FEATURE_STAT_UPDATED = "resultFeatureStatUpdated";

    @Bean
    public RecordFilterStrategy<String, Object> saveResultDataEventFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_SAVE_RESULT_DATA));
    }
    @Bean
    public RecordFilterStrategy<String, Object> saveResultStatsEventFilter() {
        return rec -> !(rec.key().equalsIgnoreCase(EVENT_SAVE_RESULT_STATS));
    }
}
