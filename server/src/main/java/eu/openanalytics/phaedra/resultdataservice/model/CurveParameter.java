package eu.openanalytics.phaedra.resultdataservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public abstract class CurveParameter {
  @Id
  private Long id;
  private Long curveId;
  private String name;
  private String stringValue;
  private Float numericValue;
}
