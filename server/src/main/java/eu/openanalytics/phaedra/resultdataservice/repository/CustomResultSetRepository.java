package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.record.ResultSetFilter;
import java.util.List;

public interface CustomResultSetRepository {
  List<ResultSet> findAllByResultSetFilter(ResultSetFilter filter);
}
