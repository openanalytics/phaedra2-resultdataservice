/**
 * Phaedra II
 * <p>
 * Copyright (C) 2016-2024 Open Analytics
 * <p>
 * ===========================================================================
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * Apache License as published by The Apache Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Apache
 * License for more details.
 * <p>
 * You should have received a copy of the Apache License along with this program.  If not, see
 * <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.Curve;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurveRepository extends CrudRepository<Curve, Long> {

  List<Curve> findCurveByPlateId(Long plateId);

  @Query("select * from curve where plate_id = :plateId AND result_set_id = (select max(result_set_id) from curve where plate_id = :plateId) order by fit_date desc")
  List<Curve> findLatestCurvesByPlateId(Long plateId);

  @Query("select * from curve where plate_id in (:plateIds) AND result_set_id = (select max(c.result_set_id) from curve c where c.plate_id = curve.plate_id) order by fit_date desc")
  List<Curve> findLatestCurvesByPlateIdIn(List<Long> plateIds);

  @Query("select * from curve where plate_id = :plateId AND result_set_id = :resultSetId order by fit_date desc")
  List<Curve> findCurvesByPlateIdAndResultSetId(Long plateId, Long resultSetId);

  @Query("select * from curve where substance_name = :substanceName order by  fit_date desc")
  List<Curve> findCurvesBySubstanceName(String substanceName);

  @Query("select * from curve where substance_type = :substanceType order by  fit_date desc")
  List<Curve> findCurvesBySubstanceType(String substanceType);

  @Query("select * from curve where feature_id = :featureId order by fit_date desc")
  List<Curve> findCurvesByFeatureId(long featureId);

  @Query("select * from curve where :wellId = ANY (wells)")
  List<Curve> findCurvesThatIncludesWellId(long wellId);

  @Query("select * from curve where wells && array[:wellIds]::bigint[] and result_set_id = (select max(result_set_id) from curve where wells && array[:wellIds]::bigint[]) order by fit_date desc")
  List<Curve> findLatestCurvesByWellIds(List<Long> wellIds);

  @Query("select * from curve where wells && array[:wellIds]::bigint[] and result_set_id = :resultSetId order by fit_date desc")
  List<Curve> findCurvesByWellIdsAndResultSetId(List<Long> wellIds, Long resultSetId);
}
