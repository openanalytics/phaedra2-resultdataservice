package eu.openanalytics.phaedra.resultdataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
