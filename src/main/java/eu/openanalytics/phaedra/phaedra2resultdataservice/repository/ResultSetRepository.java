package eu.openanalytics.phaedra.phaedra2resultdataservice.repository;

import eu.openanalytics.phaedra.phaedra2resultdataservice.model.ResultSet;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ResultSetRepository extends PagingAndSortingRepository<ResultSet, Long> {

    @Override
    @NonNull
    List<ResultSet> findAll();

}

