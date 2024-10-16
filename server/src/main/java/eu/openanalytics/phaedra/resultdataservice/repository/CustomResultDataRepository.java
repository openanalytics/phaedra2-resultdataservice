package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.record.ResultDataFilter;
import java.util.List;

public interface CustomResultDataRepository {
  List<ResultData> findAllByResultDataFilter(ResultDataFilter filter);
}
