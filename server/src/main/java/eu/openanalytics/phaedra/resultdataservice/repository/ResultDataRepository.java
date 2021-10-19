package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface ResultDataRepository extends PagingAndSortingRepository<ResultData, Long> {

    Page<ResultData> findAllByResultSetId(Pageable pageable, long resultSetId);

    Page<ResultData> findAllByResultSetIdAndFeatureId(Pageable pageable, long resultSetId, long featureId);

    List<ResultData> findByResultSetIdIn(Collection<Long> resultSetIds);
}

