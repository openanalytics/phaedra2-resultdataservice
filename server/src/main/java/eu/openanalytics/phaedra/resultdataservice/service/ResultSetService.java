package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.repository.ResultSetRepository;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class ResultSetService {

    private final ResultSetRepository resultSetRepository;
    private final Clock clock;
    private final ModelMapper modelMapper;

    public ResultSetService(ResultSetRepository resultSetRepository, Clock clock, ModelMapper modelMapper) {
        this.resultSetRepository = resultSetRepository;
        this.clock = clock;
        this.modelMapper = modelMapper;
    }

    public ResultSetDTO create(ResultSetDTO resultSetDTO) {
        var resultSet = modelMapper.map(resultSetDTO)
            .executionStartTimeStamp(LocalDateTime.now(clock))
            .build();

        return save(resultSet);
    }

    public ResultSetDTO updateOutcome(ResultSetDTO resultSetDTO) throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(resultSetDTO.getId());
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(resultSetDTO.getId());
        }
        if (existingResultSet.get().getOutcome() != null || existingResultSet.get().getExecutionEndTimeStamp() != null) {
            throw new ResultSetAlreadyCompletedException();
        }

        ResultSet resultSet = modelMapper.map(resultSetDTO, existingResultSet.get())
            .executionEndTimeStamp(LocalDateTime.now(clock))
            .build();

        return save(resultSet);
    }

    public void delete(Long id) throws ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(id);
        }
        resultSetRepository.deleteById(id);
    }

    public ResultSetDTO getResultSetById(Long id) throws ResultSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultSetNotFoundException(id);
        }
        return modelMapper.map(existingResultSet.get()).build();
    }

    public Page<ResultSetDTO> getPagedResultSets(int pageNumber) {
        var res = resultSetRepository.findAll(PageRequest.of(pageNumber, 20, Sort.Direction.ASC, "id"));
        return res.map((r) -> (modelMapper.map(r).build()));
    }

    public boolean exists(long resultSetId) {
        return resultSetRepository.existsById(resultSetId);
    }

    /**
     * Saves a {@link ResultSet} and returns the resulting corresponding {@link ResultSetDTO}.
     */
    private ResultSetDTO save(ResultSet resultSet) {
        ResultSet newResultSet = resultSetRepository.save(resultSet);
        return modelMapper.map(newResultSet).build();
    }

}
