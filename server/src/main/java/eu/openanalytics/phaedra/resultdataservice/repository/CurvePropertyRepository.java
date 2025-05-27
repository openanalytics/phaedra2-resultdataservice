package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.CurveOutputParameter;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurvePropertyRepository extends CrudRepository<CurveOutputParameter, Long> {

  @Query("select * from curve_property where curve_id = :curveId")
  List<CurveOutputParameter> findCurvePropertyByCurveId(Long curveId);
}
