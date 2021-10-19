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
}

