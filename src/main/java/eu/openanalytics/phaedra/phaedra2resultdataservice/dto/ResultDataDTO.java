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

    //    @Null(groups = OnCreate.class, message = "Id must be null when creating ResultData")
    @Null(groups = OnUpdate.class, message = "Id must be specified in URL and not repeated in body")
    private Long id;

    //    @NotNull(message = "ResultId is mandatory", groups = {OnCreate.class})
//    @Null(message = "ResultId cannot be changed", groups = {OnUpdate.class})
    @Null(groups = OnCreate.class, message = "ResultSetId must be specified in URL and not repeated in body")
    private Long resultSetId;

    @NotNull(message = "FeatureId is mandatory", groups = {OnCreate.class})
//    @Null(message = "FeatureId cannot be changed", groups = {OnUpdate.class})
    private Long featureId;

    @NotNull(message = "Values is mandatory", groups = {OnCreate.class})
//    @Null(message = "resultValues cannot be changed", groups = {OnUpdate.class})
    private double[] values;

    @NotNull(message = "StatusCode is mandatory", groups = {OnCreate.class})
//    @Null(message = "resultValues cannot be changed", groups = {OnUpdate.class})
    private StatusCode statusCode;

    @NotNull(message = "StatusMessage is mandatory", groups = {OnCreate.class})
//    @Null(message = "resultValues cannot be changed", groups = {OnUpdate.class})
    @Length(groups = OnUpdate.class, max = 255, message = "StatusMessage may only contain 255 characters")
    private String statusMessage;

    @NotNull(message = "ExitCode is mandatory", groups = {OnCreate.class})
    @Min(value = 0, message = "ExitCode must be in the range [0-255]")
    @Max(value = 255, message = "ExitCode must be in the range [0-255]")
    private int exitCode;

    @Null(message = "CreatedTimestamp must be null when creating ResultData", groups = {OnCreate.class})
//    @Null(message = "ResultSavedTimestamp cannot be changed", groups = {OnUpdate.class})
    private LocalDateTime createdTimestamp;

}
