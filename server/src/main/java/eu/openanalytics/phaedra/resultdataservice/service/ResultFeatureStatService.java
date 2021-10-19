package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultFeatureStatNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultFeatureStat;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultFeatureStatRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ResultFeatureStatService {

    private final ResultFeatureStatRepository resultFeatureStatRepository;
    private final ResultSetService resultSetService;
    private final Clock clock;
    private final ModelMapper modelMapper;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public ResultFeatureStatService(ResultFeatureStatRepository resultFeatureStatRepository, ResultSetService resultSetService, DataSource dataSource, Clock clock, ModelMapper modelMapper) {
        this.resultFeatureStatRepository = resultFeatureStatRepository;
        this.resultSetService = resultSetService;
        this.clock = clock;
        this.modelMapper = modelMapper;
    }

    public List<ResultFeatureStatDTO> create(long resultSetId, List<ResultFeatureStatDTO> resultFeatureStatDTOs) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException, DuplicateResultFeatureStatException {
        var resultSet = resultSetService.getResultSetById(resultSetId);

        if (resultSet.getOutcome() != StatusCode.SCHEDULED) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot add new ResultFeatureStat to this set.");
        }

        var resultFeatureStats = resultFeatureStatDTOs
            .stream()
            .map(r -> modelMapper
                .map(r)
                .resultSetId(resultSetId)
                .createdTimestamp(LocalDateTime.now(clock))
                .build()
            ).collect(Collectors.toList());

        return save(resultFeatureStats);
    }


    public Page<ResultFeatureStatDTO> getPagedResultFeatureStats(long resultSetId, int pageNumber, Optional<Integer> pageSize) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultFeatureStatRepository.findAllByResultSetId(PageRequest.of(pageNumber, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"), resultSetId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public ResultFeatureStatDTO getResultFeatureStat(long resultSetId, long resultFeatureStatId) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }

        var res = resultFeatureStatRepository.findById(resultFeatureStatId);
        if (res.isEmpty()) {
            throw new ResultFeatureStatNotFoundException(resultFeatureStatId);
        }

        return modelMapper.map(res.get()).build();
    }

    public List<ResultFeatureStatDTO> getResultFeatureStatsByResultIds(List<Long> resultIds) {
        return resultFeatureStatRepository.findAllByResultSetIdIn(resultIds).stream().map(it -> modelMapper.map(it).build()).toList();
    }

    public Page<ResultFeatureStatDTO> getPagedResultFeatureStatByFeatureId(long resultSetId, Integer featureId, Integer page, Optional<Integer> pageSize) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultFeatureStatRepository.findAllByResultSetIdAndFeatureId(PageRequest.of(page, pageSize.orElse(DEFAULT_PAGE_SIZE), Sort.Direction.ASC, "id"), resultSetId, featureId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public void delete(long resultSetId, long resultFeatureStatId) throws ResultSetNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException, ResultFeatureStatNotFoundException {
        var resultSet = resultSetService.getResultSetById(resultSetId);
        if (resultSet.getOutcome() != StatusCode.SCHEDULED) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot delete a ResultFeatureStat from this set.");
        }
        var resultFeatureStat = resultFeatureStatRepository.findById(resultFeatureStatId);
        if (resultFeatureStat.isEmpty()) {
            throw new ResultFeatureStatNotFoundException(resultFeatureStatId);
        }
        if (resultFeatureStat.get().getResultSetId() != resultSetId) {
            throw InvalidResultSetIdException.forResultFeatureStat(resultSetId, resultFeatureStatId);
        }
        resultFeatureStatRepository.deleteById(resultFeatureStatId);
    }

    /**
     * Saves a {@link ResultFeatureStat} and returns the resulting corresponding {@link ResultFeatureStatDTO}.
     */
    private ResultFeatureStatDTO save(ResultFeatureStat resultFeatureStat) throws DuplicateResultFeatureStatException {
        try {
            return modelMapper.map(resultFeatureStatRepository.save(resultFeatureStat)).build();
        } catch (DbActionExecutionException ex) {
            if (ex.getCause() instanceof DuplicateKeyException) {
                throw new DuplicateResultFeatureStatException();
            }
            throw ex;
        }
    }

    // TODO test whether everything is rolled-back when error
    private List<ResultFeatureStatDTO> save(List<ResultFeatureStat> resultFeatureStats) throws DuplicateResultFeatureStatException {
        try {
            var entities = resultFeatureStatRepository.saveAll(resultFeatureStats).spliterator();
            return StreamSupport.stream(entities, false)
                .map(f -> modelMapper.map(f).build())
                .collect(Collectors.toList());
        } catch (DbActionExecutionException ex) {
            if (ex.getCause() instanceof DuplicateKeyException) {
                throw new DuplicateResultFeatureStatException();
            }
            throw ex;
        }
    }

}
