package eu.openanalytics.phaedra.resultdataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Configuration
@EnableKafka
public class KafkaConfig {
    // Kafka topics
    public static final String RESULTDATA_TOPIC = "resultdata-topic";

    // Kafka events
    public static final String SAVE_FEATURE_RESULTDATA_EVENT = "saveResultDataEvent";
    public static final String SAVE_FEATURE_STATS_EVENT = "saveResultStatsEvent";

    @Bean
    public RecordFilterStrategy<String, Object> saveResultDataEventFilter() {
        RecordFilterStrategy<String, Object> recordFilterStrategy = consumerRecord -> !(consumerRecord.key().equalsIgnoreCase(SAVE_FEATURE_RESULTDATA_EVENT));
        return recordFilterStrategy;
    }
    @Bean
    public RecordFilterStrategy<String, Object> saveResultStatsEventFilter() {
        RecordFilterStrategy<String, Object> recordFilterStrategy = consumerRecord -> !(consumerRecord.key().equalsIgnoreCase(SAVE_FEATURE_STATS_EVENT));
        return recordFilterStrategy;
    }
}
