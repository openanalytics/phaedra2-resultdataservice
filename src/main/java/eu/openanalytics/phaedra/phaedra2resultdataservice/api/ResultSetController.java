package eu.openanalytics.phaedra.phaedra2resultdataservice.api;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.validation.OnUpdate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetAlreadyCompletedException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultDataSetNotFoundException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.service.ResultSetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;


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
    public ResponseEntity<ResultSetDTO> createResultSet(@Validated(OnUpdate.class) @RequestBody ResultSetDTO resultSetDTO, @PathVariable Long id) throws ResultDataSetAlreadyCompletedException, ResultDataSetNotFoundException {
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

}
