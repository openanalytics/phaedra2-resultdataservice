package eu.openanalytics.phaedra.phaedra2resultdataservice.model;

import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Value
@With
public class ResultData {

    @Id
    Long id;

    Long resultSetId;

    Long featureId;

    double[] values;

    StatusCode statusCode;

    String statusMessage;

    int exitCode;

    LocalDateTime createdTimestamp;

}
