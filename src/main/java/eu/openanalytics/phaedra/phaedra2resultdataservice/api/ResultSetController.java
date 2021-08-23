package eu.openanalytics.phaedra.phaedra2resultdataservice.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetAlreadyCompletedException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetNotFoundException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.UserVisibleException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.service.ResultSetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@Validated
public class ResultSetController {

    private final ResultSetService resultSetService;

    public ResultSetController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @ResponseBody
    @PostMapping("/resultset")
    public ResponseEntity<ResultSetDTO> createResultSet(@Validated(OnCreate.class) @RequestBody ResultSetDTO resultSetDTO) {
        var res = resultSetService.create(resultSetDTO);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @ResponseBody
    @PutMapping("/resultset/{id}")
    public ResponseEntity<ResultSetDTO> updateResultSet(@Validated(OnUpdate.class) @RequestBody ResultSetDTO resultSetDTO, @PathVariable Long id) throws ResultDataSetAlreadyCompletedException, ResultDataSetNotFoundException {
        resultSetDTO.setId(id);
        var res = resultSetService.updateOutcome(resultSetDTO);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/resultset/{id}")
    public ResponseEntity<ResultSetDTO> deleteResultSet(@PathVariable Long id) throws ResultDataSetNotFoundException {
        resultSetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @GetMapping("/resultset/{id}")
    public ResponseEntity<ResultSetDTO> getResultSet(@PathVariable Long id) throws ResultDataSetNotFoundException {
        var res = resultSetService.getResultSetById(id);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/resultset")
    public HashMap<String, Object> getAllResultSets(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page) {
        var pages = resultSetService.getPagedResultSets(page);
        return new HashMap<>() {{
            put("data", pages.getContent());
            put("status", new HashMap<String, Object>() {{
                put("totalPages", pages.getTotalPages());
                put("totalElements", pages.getTotalElements());
                put("first", pages.isFirst());
                put("last", pages.isLast());
            }});
        }};
    }

    // TODO move to phaedra-commons ?
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserVisibleException.class)
    public HashMap<String, Object> handleValidationExceptions(UserVisibleException ex) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", ex.getMessage());
        }};
    }

    // TODO move to phaedra-commons ?
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HashMap<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", "Validation error");
            put("malformed_fields", ex.getBindingResult()
                .getAllErrors()
                .stream().
                collect(Collectors.toMap(
                    error -> ((FieldError) error).getField(),
                    error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Field is invalid"))
                )
            );
        }};
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public HashMap<String, Object> handleValidationExceptions(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException cause = (InvalidFormatException) ex.getCause();
            String fieldName = cause.getPath().get(0).getFieldName();

            return new HashMap<>() {{
                put("status", "error");
                put("error", "Validation error");
                put("malformed_fields", new HashMap<>() {{
                    put(fieldName, "Invalid value provided");
                }});
            }};
        }
        return new HashMap<>() {{
            put("status", "error");
            put("error", "Validation error");
        }};
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResultDataSetNotFoundException.class)
    public HashMap<String, Object> handleValidationExceptions(ResultDataSetNotFoundException ex) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", ex.getMessage());
        }};
    }
}
