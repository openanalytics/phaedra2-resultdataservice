package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.CurveInputParameter;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface CurveInputParameterRepository extends CrudRepository<CurveInputParameter, Long> {

  @Query("select * from curve_input_parameter where curve_id = :curveId")
  List<CurveInputParameter> findCurveOutputParametersByCurveId(Long curveId);

  @Query("select * from curve_input_parameter where curve_id = :curveId and name = :name")
  CurveInputParameter findCurveInputParameterByCurveIdAndName(Long curveId, String name);

}
