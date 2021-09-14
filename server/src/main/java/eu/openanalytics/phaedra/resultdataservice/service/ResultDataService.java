package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultDataNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultDataRepository;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class ResultDataService {

    private final ResultDataRepository resultDataRepository;
    private final ResultSetService resultSetService;
    private final DataSource dataSource;
    private final Clock clock;
    private final ModelMapper modelMapper;

    public ResultDataService(ResultDataRepository resultDataRepository, ResultSetService resultSetService, DataSource dataSource, Clock clock, ModelMapper modelMapper) {
        this.resultDataRepository = resultDataRepository;
        this.resultSetService = resultSetService;
        this.dataSource = dataSource;
        this.clock = clock;
        this.modelMapper = modelMapper;
    }

    public ResultDataDTO create(long resultSetId, ResultDataDTO resultDataDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException {
        var resultSet = resultSetService.getResultSetById(resultSetId);

        if (resultSet.getOutcome() != null) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot add new ResultData to this set.");
        }

        ResultData resultData = modelMapper
            .map(resultDataDTO)
            .resultSetId(resultSetId)
            .createdTimestamp(LocalDateTime.now(clock))
            .build();

        return save(resultData);
    }

    public Page<ResultDataDTO> getPagedResultData(long resultSetId, int pageNumber) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultDataRepository.findAllByResultSetId(PageRequest.of(pageNumber, 20, Sort.Direction.ASC, "id"), resultSetId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public ResultDataDTO getResultData(long resultSetId, long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }

        var res = resultDataRepository.findById(resultDataId);
        if (res.isEmpty()) {
            throw new ResultDataNotFoundException(resultDataId);
        }

        return modelMapper.map(res.get()).build();
    }

    public Page<ResultDataDTO> getPagedResultDataByFeatureId(long resultSetId, Integer featureId, Integer page) throws ResultSetNotFoundException {
        if (!resultSetService.exists(resultSetId)) {
            throw new ResultSetNotFoundException(resultSetId);
        }
        var res = resultDataRepository.findAllByResultSetIdAndFeatureId(PageRequest.of(page, 20, Sort.Direction.ASC, "id"), resultSetId, featureId);
        return res.map((r) -> modelMapper.map(r).build());
    }

    public void delete(long resultSetId, long resultDataId) throws ResultSetNotFoundException, ResultDataNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException {
        var resultSet = resultSetService.getResultSetById(resultSetId);
        if (resultSet.getOutcome() != null) {
            throw new ResultSetAlreadyCompletedException("ResultSet is already completed, cannot delete a ResultData from this set.");
        }
        var resultData = resultDataRepository.findById(resultDataId);
        if (resultData.isEmpty()) {
            throw new ResultDataNotFoundException(resultDataId);
        }
        if (resultData.get().getResultSetId() != resultSetId) {
            throw new InvalidResultSetIdException(resultSetId, resultDataId);
        }
        resultDataRepository.deleteById(resultDataId);
    }

    /**
     * Saves a {@link ResultData} and returns the resulting corresponding {@link ResultDataDTO}.
     */
    private ResultDataDTO save(ResultData resultData) {
        // workaround for https://github.com/spring-projects/spring-data-jdbc/issues/1033
        var simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("result_data").usingGeneratedKeyColumns("id");

        Number id = simpleJdbcInsert.executeAndReturnKey(new HashMap<>() {{
            put("result_set_id", resultData.getResultSetId());
            put("feature_id", resultData.getFeatureId());
            put("values", resultData.getValues());
            put("status_code", resultData.getStatusCode());
            put("status_message", resultData.getStatusMessage());
            put("exit_code", resultData.getExitCode());
            put("created_timestamp", resultData.getCreatedTimestamp());
        }});

        return modelMapper.map(resultDataRepository.findById(id.longValue()).get()).build();
    }

}
