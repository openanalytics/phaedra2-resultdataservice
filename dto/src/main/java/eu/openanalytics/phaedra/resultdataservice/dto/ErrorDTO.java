package eu.openanalytics.phaedra.resultdataservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

import java.time.LocalDateTime;

@Value
@Builder
@With
@AllArgsConstructor
@NonFinal
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
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
    Integer exitCode;
    String statusMessage;

    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(String.format(" - Timestamp: [%s]", getTimestamp()));
        if (getExceptionClassName() != null) {
            description.append(String.format(", Exception: [%s: %s]", getExceptionClassName(), getExceptionMessage()));
        }
        description.append(String.format(", Description: [%s]", getDescription()));
        if (getFeatureId() != null) {
            description.append(String.format(", Feature: [%s %s], Sequence: [%s], Formula: [%s %s]", getFeatureId(), getFeatureName(), getSequenceNumber(), getFormulaId(), getFormulaName()));
        }
        if (getCivType() != null) {
            description.append(String.format(", CivType: [%s], CivSource: [%s], CivVariableName: [%s]", getCivType(), getCivSource(), getCivVariableName()));
        }
        if (getExitCode() != null) {
            description.append(String.format(", ExitCode: [%s]", getExitCode()));
        }
        if (getStatusMessage() != null) {
            description.append(String.format(", StatusMessage: [%s]", getStatusMessage()));
        }
        return description.toString();
    }
}
