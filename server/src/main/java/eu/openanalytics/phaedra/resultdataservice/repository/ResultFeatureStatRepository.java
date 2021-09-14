package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.ResultFeatureStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResultFeatureStatRepository extends PagingAndSortingRepository<ResultFeatureStat, Long> {

    Page<ResultFeatureStat> findAllByResultSetId(Pageable pageable, long resultSetId);

    Page<ResultFeatureStat> findAllByResultSetIdAndFeatureId(Pageable pageable, long resultSetId, long featureId);

}

