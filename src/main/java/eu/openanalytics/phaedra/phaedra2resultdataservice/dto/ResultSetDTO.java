package eu.openanalytics.phaedra.phaedra2resultdataservice.dto;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnUpdate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Value
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
public class ResultSetDTO {

    @Null(groups = OnCreate.class, message = "Id must be null when creating a ResultSet")
    @Null(groups = OnUpdate.class, message = "Id must be specified in URL and not repeated in body")
    Long id;

    @NotNull(message = "ProtocolId is mandatory", groups = {OnCreate.class})
    @Null(message = "ProtocolId cannot be changed", groups = {OnUpdate.class})
    Long protocolId;

    @NotNull(message = "PlateId is mandatory", groups = {OnCreate.class})
    @Null(message = "PlateId cannot be changed", groups = {OnUpdate.class})
    Long plateId;

    @NotNull(message = "MeasId is mandatory", groups = {OnCreate.class})
    @Null(message = "MeasId cannot be changed", groups = {OnUpdate.class})
    Long measId;

    @Null(message = "ExecutionStartTimeStamp must be null when creating a ResultSet", groups = {OnCreate.class})
    @Null(message = "ExecutionStartTimeStamp cannot be changed", groups = {OnUpdate.class})
    LocalDateTime executionStartTimeStamp;

    @Null(message = "ExecutionEndTimeStamp must be null when creating a ResultSet", groups = {OnCreate.class})
    @Null(message = "ExecutionEndTimeStamp cannot be changed", groups = {OnUpdate.class})
    LocalDateTime executionEndTimeStamp;

    @Null(groups = OnCreate.class, message = "Outcome must be null when creating a ResultSet")
    @NotNull(groups = OnUpdate.class, message = "Outcome is mandatory when updating a ResultSet")
    @Length(groups = OnUpdate.class, max = 255, message = "Outcome may only contain 255 characters")
    String outcome;

}
