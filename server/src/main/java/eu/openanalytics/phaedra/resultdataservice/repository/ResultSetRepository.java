/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ResultSetRepository extends PagingAndSortingRepository<ResultSet, Long> {

    Page<ResultSet> findAllByOutcome(Pageable pageable, ResultSet.StatusCodeHolder outcome);

    default Page<ResultSet> findAllByOutcome(Pageable pageable, StatusCode outcome) {
        return findAllByOutcome(pageable, new ResultSet.StatusCodeHolder(outcome));
    }

    List<ResultSet> findAllByPlateId(Long plateId);

    List<ResultSet> findByPlateIdAndMeasId(Long plateId, Long measId);

    @Query("SELECT * FROM result_set WHERE id in (SELECT MAX(id) FROM resultdataservice.result_set WHERE plate_id = :plateId GROUP BY (protocol_id))")
    List<ResultSet> findLatestByPlateId(Long plateId);

    @Query("SELECT * FROM result_set WHERE id in (SELECT MAX(id) FROM resultdataservice.result_set WHERE plate_id = :plateId and meas_id = :measId GROUP BY (protocol_id))")
    List<ResultSet> findLatestPlateIdAndMeasId(Long plateId, Long measId);

    @Query("SELECT * FROM result_set ORDER BY execution_start_time_stamp DESC LIMIT :n")
    List<ResultSet> findNMostRecentResultSets(Integer n);
}

