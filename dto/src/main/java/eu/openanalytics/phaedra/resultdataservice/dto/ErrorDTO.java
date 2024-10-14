/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

@Value
@Builder
@With
@AllArgsConstructor
@NonFinal
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDTO {

    LocalDateTime timestamp;
    String exceptionClassName;
    String exceptionMessage;
    String description;
    Long featureId;
    String featureName;
    Integer sequenceNumber;
    Long formulaId;
    String formulaName;
    String civType;
    String civVariableName;
    String civSource;
    String statusMessage;
    Long featureStatId;
    String featureStatName;
    Long newResultSetId; // the id of the ResultSet that retries this calculation

    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(String.format(" - Timestamp: [%s]", getTimestamp()));
        if (getExceptionClassName() != null) {
            description.append(String.format(", Exception: [%s: %s]", getExceptionClassName(), getExceptionMessage()));
        }
        description.append(String.format(", Description: [%s]", getDescription()));
        if (getFeatureId() != null) {
            description.append(String.format(", Feature: [%s %s], Sequence: [%s]", getFeatureId(), getFeatureName(), getSequenceNumber()));
        }
        if (getFormulaId() != null) {
            description.append(String.format(", Formula: [%s %s]", getFormulaId(), getFormulaName()));
        }
        if (getCivType() != null) {
            description.append(String.format(", CivType: [%s], CivSource: [%s], CivVariableName: [%s]", getCivType(), getCivSource(), getCivVariableName()));
        }
        if (getStatusMessage() != null) {
            description.append(String.format(", StatusMessage: [%s]", getStatusMessage()));
        }
        if (getFeatureStatId() != null) {
            description.append(String.format(", FeatureStat: [%s %s]", getFeatureStatId(), getFeatureStatName()));
        }
        if (getNewResultSetId() != null) {
            description.append(String.format(", Id of new ResultSet: [%s]", getNewResultSetId()));
        }
        return description.toString();
    }
}
