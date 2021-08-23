package eu.openanalytics.phaedra.phaedra2resultdataservice.dto;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultSetDTO {

    @Null(groups = OnCreate.class, message = "Id must be null when creating a formula")
    @Null(groups = OnUpdate.class, message = "Id must be specified in URL and not repeated in body")
    private Long id;

    @NotNull(message = "ProtocolId is mandatory", groups = {OnCreate.class})
    @Null(message = "ProtocolId cannot be changed", groups = {OnUpdate.class})
    private Long protocolId;

    @NotNull(message = "PlateId is mandatory", groups = {OnCreate.class})
    @Null(message = "PlateId cannot be changed", groups = {OnUpdate.class})
    private Long plateId;

    @NotNull(message = "MeasId is mandatory", groups = {OnCreate.class})
    @Null(message = "MeasId cannot be changed", groups = {OnUpdate.class})
    private Long measId;

    @Null(message = "ExecutionStartTimeStamp must be null when creating a formula", groups = {OnCreate.class, OnUpdate.class})
    @Null(message = "ExecutionStartTimeStamp cannot be changed", groups = {OnUpdate.class})
    private LocalDateTime executionStartTimeStamp;

    @Null(message = "ExecutionEndTimeStamp must be null when creating a formula", groups = {OnCreate.class, OnUpdate.class})
    @Null(message = "ExecutionEndTimeStamp cannot be changed", groups = {OnUpdate.class})
    private LocalDateTime executionEndTimeStamp;

    @Null(groups = OnCreate.class, message = "Outcome must be null when creating a formula")
    @NotNull(groups = OnUpdate.class, message = "Outcome is mandatory when updating a formula")
    @Length(groups = OnUpdate.class, max = 255, message = "Outcome may only contain 255 characters")
    private String outcome;

}
