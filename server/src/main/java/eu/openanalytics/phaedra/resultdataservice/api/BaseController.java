package eu.openanalytics.phaedra.resultdataservice.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import eu.openanalytics.phaedra.resultdataservice.exception.EntityNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.UserVisibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO move to phaedra-commons ?
public class BaseController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserVisibleException.class)
    public HashMap<String, Object> handleValidationExceptions(UserVisibleException ex) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", ex.getMessage());
        }};
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public HashMap<String, Object> handleValidationExceptions(EntityNotFoundException ex) {
        return handleValidationExceptions((UserVisibleException) ex);
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

}
