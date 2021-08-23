package eu.openanalytics.phaedra.phaedra2resultdataservice.service;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetAlreadyCompletedException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetNotFoundException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.phaedra2resultdataservice.repository.ResultSetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;


@Service
public class ResultSetService {

    private final ResultSetRepository resultSetRepository;

    public ResultSetService(ResultSetRepository resultSetRepository) {
        this.resultSetRepository = resultSetRepository;
    }

    public ResultSetDTO create(ResultSetDTO resultSetDTO) {
        var resultSet = new ResultSet(
            resultSetDTO.getId(),
            resultSetDTO.getProtocolId(),
            resultSetDTO.getPlateId(),
            resultSetDTO.getMeasId(),
            LocalDate.now(),
            null,
            null);

        return save(resultSet);
    }

    public ResultSetDTO updateOutcome(ResultSetDTO resultSetDTO) throws ResultDataSetAlreadyCompletedException, ResultDataSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(resultSetDTO.getId());
        if (existingResultSet.isEmpty()) {
            throw new ResultDataSetNotFoundException(resultSetDTO.getId());
        }
        if (existingResultSet.get().getOutcome() != null || existingResultSet.get().getExecutionEndTimeStamp() != null) {
            throw new ResultDataSetAlreadyCompletedException();
        }
        var resultSet = existingResultSet.get()
            .withOutcome(resultSetDTO.getOutcome())
            .withExecutionEndTimeStamp(LocalDate.now());
        return save(resultSet);
    }

    public void delete(Long id) throws ResultDataSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultDataSetNotFoundException(id);
        }
        resultSetRepository.deleteById(id);
    }

    public ResultSetDTO getResultSetById(Long id) throws ResultDataSetNotFoundException {
        Optional<ResultSet> existingResultSet = resultSetRepository.findById(id);
        if (existingResultSet.isEmpty()) {
            throw new ResultDataSetNotFoundException(id);
        }
        return map(existingResultSet.get());
    }

    public Page<ResultSetDTO> getPagedResultSets(int pageNumber) {
        var res = resultSetRepository.findAll(PageRequest.of(pageNumber, 20, Sort.Direction.ASC, "id"));
        return res.map(this::map);
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
