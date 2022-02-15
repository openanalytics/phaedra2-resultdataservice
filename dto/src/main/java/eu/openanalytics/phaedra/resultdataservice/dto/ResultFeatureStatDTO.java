/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.dto;

import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@With
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
@NonFinal
public class ResultFeatureStatDTO {

    @Null(groups = OnCreate.class, message = "Id must be null when creating a ResultFeatureStat")
    Long id;

    @Null(groups = OnCreate.class, message = "ResultSetId must be specified in URL and not repeated in body")
    Long resultSetId;

    @NotNull(message = "FeatureId is mandatory", groups = {OnCreate.class})
    Long featureId;

    @NotNull(message = "FeatureStatId is mandatory", groups = {OnCreate.class})
    Long featureStatId;

    Float value; // may be null when there is no result computed

    @NotNull(message = "StatisticName is mandatory", groups = {OnCreate.class})
    String statisticName;

    String welltype;

    @NotNull(message = "StatusCode is mandatory", groups = {OnCreate.class})
    StatusCode statusCode;

    @NotNull(message = "StatusMessage is mandatory", groups = {OnCreate.class})
    @Length(max = 255, message = "StatusMessage may only contain 255 characters", groups = OnCreate.class)
    String statusMessage;

    @NotNull(message = "ExitCode is mandatory", groups = {OnCreate.class})
    @Min(value = 0, message = "ExitCode must be in the range [0-255]", groups = OnCreate.class)
    @Max(value = 255, message = "ExitCode must be in the range [0-255]", groups = OnCreate.class)
    Integer exitCode;

    @Null(message = "CreatedTimestamp must be null when creating ResultFeatureSta", groups = {OnCreate.class})
    LocalDateTime createdTimestamp;
}
