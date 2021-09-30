package eu.openanalytics.phaedra.resultdataservice.dto;

import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
@NonFinal
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
    StatusCode outcome;

    @Null(groups = OnCreate.class, message = "Errors must be null when creating a ResultSet")
    @NotNull(groups = OnUpdate.class, message = "Errors is mandatory when updating a ResultSet")
    List<ErrorDTO> errors;

    @Null(groups = OnCreate.class, message = "ErrorsText must be null when creating a ResultSet")
    @NotNull(groups = OnUpdate.class, message = "ErrorsText is mandatory when updating a ResultSet")
    String errorsText;
}
