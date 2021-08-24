package eu.openanalytics.phaedra.phaedra2resultdataservice.service;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.phaedra2resultdataservice.repository.ResultSetRepository;
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

    public ResultSetService(ResultSetRepository resultSetRepository, Clock clock) {
        this.resultSetRepository = resultSetRepository;
        this.clock = clock;
    }

    public ResultSetDTO create(ResultSetDTO resultSetDTO) {
        var resultSet = new ResultSet(
            null,
            resultSetDTO.getProtocolId(),
            resultSetDTO.getPlateId(),
            resultSetDTO.getMeasId(),
            LocalDateTime.now(clock),
            null,
            null);

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
        var resultSet = existingResultSet.get()
            .withOutcome(resultSetDTO.getOutcome())
            .withExecutionEndTimeStamp(LocalDateTime.now(clock));
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
        return map(existingResultSet.get());
    }

    public Page<ResultSetDTO> getPagedResultSets(int pageNumber) {
        var res = resultSetRepository.findAll(PageRequest.of(pageNumber, 20, Sort.Direction.ASC, "id"));
        return res.map(this::map);
    }

    public boolean exists(long resultSetId) {
        return resultSetRepository.existsById(resultSetId);
    }

    /**
     * Convenience-function to convert Entity to DTO.
     */
    private ResultSetDTO map(ResultSet resultSet) {
        return new ResultSetDTO(
            resultSet.getId(),
            resultSet.getProtocolId(),
            resultSet.getPlateId(),
            resultSet.getMeasId(),
            resultSet.getExecutionStartTimeStamp(),
            resultSet.getExecutionEndTimeStamp(),
            resultSet.getOutcome()
        );
    }

    /**
     * Saves a {@link ResultSet} and returns the resulting corresponding {@link ResultSetDTO}.
     */
    private ResultSetDTO save(ResultSet resultSet) {
        ResultSet newResultSet = resultSetRepository.save(resultSet);
        return map(newResultSet);
    }

}
