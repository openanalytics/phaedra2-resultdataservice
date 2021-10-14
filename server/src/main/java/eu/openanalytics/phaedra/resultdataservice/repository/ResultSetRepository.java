package eu.openanalytics.phaedra.resultdataservice.repository;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResultSetRepository extends PagingAndSortingRepository<ResultSet, Long> {

    Page<ResultSet> findAllByOutcome(Pageable pageable, ResultSet.StatusCodeHolder outcome);

    default Page<ResultSet> findAllByOutcome(Pageable pageable, StatusCode outcome) {
        return findAllByOutcome(pageable, new ResultSet.StatusCodeHolder(outcome));
    }
}

