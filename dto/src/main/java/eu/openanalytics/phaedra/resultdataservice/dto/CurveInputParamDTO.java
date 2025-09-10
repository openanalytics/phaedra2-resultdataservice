package eu.openanalytics.phaedra.resultdataservice.dto;

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
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE) // Jackson deserialize compatibility
@NonFinal
public class CurveInputParamDTO {
  Long curveId;
  String name;
  String stringValue;
  Float numericValue;
  Boolean booleanValue;

  public boolean isNumeric() {
    return numericValue != null && stringValue == null && booleanValue == null;
  }

  public boolean isString() {
    return stringValue != null && numericValue == null && booleanValue == null;
  }

  public boolean isBoolean() {
    return booleanValue != null && stringValue == null && numericValue == null;
  }
}
