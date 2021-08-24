package eu.openanalytics.phaedra.phaedra2resultdataservice.repository;

import eu.openanalytics.phaedra.phaedra2resultdataservice.model.ResultData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResultDataRepository extends PagingAndSortingRepository<ResultData, Long> {

    Page<ResultData> findAllByResultSetId(Pageable pageable, long resultSetId);

}

