package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
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

@Service
public class ResultFeatureStatService {

    private final ResultFeatureStatRepository resultFeatureStatRepository;
    private final ResultSetService resultSetService;
    private final Clock clock;
    private final ModelMapper modelMapper;

    public ResultFeatureStatService(ResultFeatureStatRepository resultFeatureStatRepository, ResultSetService resultSetService, DataSource dataSource, Clock clock, ModelMapper modelMapper) {
        this.resultFeatureStatRepository = resultFeatureStatRepository;
        this.resultSetService = resultSetService;
        this.clock = clock;
        this.modelMapper = modelMapper;
    }

    public ResultFeatureStatDTO create(long resultSetId, ResultFeatureStatDTO resultFeatureStatDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException, DuplicateResultFeatureStatException {
        var resultSet = resultSetService.getResultSetById(resultSetId);

        if (resultSet.getOutcome() != null) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot add new ResultFeatureStat to this set.");
        }

        var resultFeatureStat = modelMapper
            .map(resultFeatureStatDTO)
            .resultSetId(resultSetId)
            .createdTimestamp(LocalDateTime.now(clock))
            .build();

        return save(resultFeatureStat);
    }

    public Page<ResultFeatureStatDTO> getPagedResultFeatureStats(long resultSetId, int pageNumber) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultFeatureStatRepository.findAllByResultSetId(PageRequest.of(pageNumber, 20, Sort.Direction.ASC, "id"), resultSetId);
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

    public Page<ResultFeatureStatDTO> getPagedResultFeatureStatByFeatureId(long resultSetId, Integer featureId, Integer page) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultFeatureStatRepository.findAllByResultSetIdAndFeatureId(PageRequest.of(page, 20, Sort.Direction.ASC, "id"), resultSetId, featureId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public void delete(long resultSetId, long resultFeatureStatId) throws ResultSetNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException, ResultFeatureStatNotFoundException {
        var resultSet = resultSetService.getResultSetById(resultSetId);
        if (resultSet.getOutcome() != null) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot delete a ResultFeatureStat from this set.");
        }
        var resultFeatureStat = resultFeatureStatRepository.findById(resultFeatureStatId);
        if (resultFeatureStat.isEmpty()) {
            throw new ResultFeatureStatNotFoundException(resultFeatureStatId);
        }
        if (resultFeatureStat.get().getResultSetId() != resultSetId) {
            throw new InvalidResultSetIdException(resultSetId, resultFeatureStatId); // TODO wrong message
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

}