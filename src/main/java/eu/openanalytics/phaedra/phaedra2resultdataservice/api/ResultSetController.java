package eu.openanalytics.phaedra.phaedra2resultdataservice.api;

import eu.openanalytics.phaedra.model.v2.dto.PageDTO;
import eu.openanalytics.phaedra.model.v2.dto.ResultSetDTO;
import eu.openanalytics.phaedra.model.v2.validation.OnCreate;
import eu.openanalytics.phaedra.model.v2.validation.OnUpdate;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.phaedra2resultdataservice.service.ResultSetService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated
public class ResultSetController extends BaseController {

    private final ResultSetService resultSetService;

    public ResultSetController(ResultSetService resultSetService) {
        this.resultSetService = resultSetService;
    }

    @ResponseBody
    @PostMapping(path = "/resultset", produces = {"application/json"}, consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public ResultSetDTO createResultSet(@Validated(OnCreate.class) @RequestBody ResultSetDTO resultSetDTO) {
        return resultSetService.create(resultSetDTO);
    }

    @ResponseBody
    @PutMapping(path = "/resultset/{id}", produces = {"application/json"}, consumes = {"application/json"})
    public ResultSetDTO updateResultSet(@Validated(OnUpdate.class) @RequestBody ResultSetDTO resultSetDTO, @PathVariable Long id) throws ResultSetAlreadyCompletedException, ResultSetNotFoundException {
        return resultSetService.updateOutcome(resultSetDTO.withId(id));
    }

    @DeleteMapping("/resultset/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        resultSetService.delete(id);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{id}", produces = {"application/json"})
    public ResultSetDTO getResultSet(@PathVariable Long id) throws ResultSetNotFoundException {
        return resultSetService.getResultSetById(id);
    }

    @ResponseBody
    @GetMapping(path = "/resultset", produces = {"application/json"})
    public PageDTO<ResultSetDTO> getAllResultSets(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page) {
        var pages = resultSetService.getPagedResultSets(page);
        return PageDTO.map(pages);
    }

}
