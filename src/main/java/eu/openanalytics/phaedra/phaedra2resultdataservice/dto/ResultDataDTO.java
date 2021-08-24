package eu.openanalytics.phaedra.phaedra2resultdataservice.dto;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.model.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDataDTO {

    @Null(message = "Id must be specified in URL and not repeated in body", groups = OnCreate.class)
    private Long id;

    @Null(message = "ResultSetId must be specified in URL and not repeated in body", groups = OnCreate.class)
    private Long resultSetId;

    @NotNull(message = "FeatureId is mandatory", groups = {OnCreate.class})
    private Long featureId;

    @NotNull(message = "Values is mandatory", groups = {OnCreate.class})
    private double[] values;

    @NotNull(message = "StatusCode is mandatory", groups = {OnCreate.class})
    private StatusCode statusCode;

    @NotNull(message = "StatusMessage is mandatory", groups = {OnCreate.class})
    @Length(max = 255, message = "StatusMessage may only contain 255 characters", groups = OnCreate.class)
    private String statusMessage;

    @NotNull(message = "ExitCode is mandatory", groups = {OnCreate.class})
    @Min(value = 0, message = "ExitCode must be in the range [0-255]", groups = OnCreate.class)
    @Max(value = 255, message = "ExitCode must be in the range [0-255]", groups = OnCreate.class)
    private Integer exitCode;

    @Null(message = "CreatedTimestamp must be null when creating ResultData", groups = {OnCreate.class})
    private LocalDateTime createdTimestamp;

}
