/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
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

import eu.openanalytics.phaedra.resultdataservice.model.ResultFeatureStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface ResultFeatureStatRepository extends PagingAndSortingRepository<ResultFeatureStat, Long> {

    List<ResultFeatureStat> findAllByResultSetId(long resultSetId);

    Page<ResultFeatureStat> findAllByResultSetId(Pageable pageable, long resultSetId);

    Page<ResultFeatureStat> findAllByResultSetIdAndFeatureId(Pageable pageable, long resultSetId, long featureId);

    List<ResultFeatureStat> findAllByResultSetIdIn(Collection<Long> resultIds);
}

