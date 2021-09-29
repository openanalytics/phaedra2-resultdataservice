package eu.openanalytics.phaedra.resultdataservice.model;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
@Builder
@With
@AllArgsConstructor
@NonFinal
public class ResultFeatureStat {

    @Id
    Long id;

    Long resultSetId;

    Long featureId;

    Long featureStatId;

    Float value;

    String statisticName;

    String welltype;

    StatusCode statusCode;

    String statusMessage;

    Integer exitCode;

    LocalDateTime createdTimestamp;

}
