package eu.openanalytics.phaedra.phaedra2resultdataservice.model;

import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Value
@With
public class ResultSet {

    @Id
    Long id;

    Long protocolId;

    Long plateId;

    Long measId;

    LocalDate executionStartTimeStamp;

    LocalDate executionEndTimeStamp;

    String outcome;

}
