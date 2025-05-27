package eu.openanalytics.phaedra.resultdataservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data()
@NoArgsConstructor()
public class CurveInputParameter extends CurveParameter {
  @Column("curve_id")
  private Long curveId;
  @Column("name")
  private String name;
  @Column("string_value")
  private String stringValue;
  @Column("numeric_value")
  private Float numericValue;
}
