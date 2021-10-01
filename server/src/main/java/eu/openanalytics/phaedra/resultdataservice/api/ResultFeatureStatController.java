package eu.openanalytics.phaedra.resultdataservice.api;

import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.validation.OnCreate;
import eu.openanalytics.phaedra.resultdataservice.exception.DuplicateResultFeatureStatException;
import eu.openanalytics.phaedra.resultdataservice.exception.InvalidResultSetIdException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultFeatureStatNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetAlreadyCompletedException;
import eu.openanalytics.phaedra.resultdataservice.exception.ResultSetNotFoundException;
import eu.openanalytics.phaedra.resultdataservice.service.ResultFeatureStatService;
import org.springframework.data.domain.Page;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@Validated
public class ResultFeatureStatController extends BaseController {

    private final ResultFeatureStatService resultFeatureStatService;

    public ResultFeatureStatController(ResultFeatureStatService resultFeatureStatService) {
        this.resultFeatureStatService = resultFeatureStatService;
    }


    @ResponseBody
    @PostMapping(path = "/resultset/{resultSetId}/resultfeaturestat", produces = {"application/json"}, consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResultFeatureStatDTO> createResultFeatureStat(@PathVariable long resultSetId, @Validated(OnCreate.class) @RequestBody List<ResultFeatureStatDTO> resultFeatureStatDTO) throws ResultSetNotFoundException, ResultSetAlreadyCompletedException, DuplicateResultFeatureStatException {
        return resultFeatureStatService.create(resultSetId, resultFeatureStatDTO);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{resultSetId}/resultfeaturestat", produces = {"application/json"})
    public PageDTO<ResultFeatureStatDTO> getResultFeatureStat(@PathVariable long resultSetId,
                                                              @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                              @RequestParam(name = "pageSize", required = false) Optional<Integer> pageSize,
                                                              @RequestParam(name = "featureId", required = false) Integer featureId) throws ResultSetNotFoundException {
        Page<ResultFeatureStatDTO> pages;
        if (featureId == null) {
            pages = resultFeatureStatService.getPagedResultFeatureStats(resultSetId, page, pageSize);
        } else {
            pages = resultFeatureStatService.getPagedResultFeatureStatByFeatureId(resultSetId, featureId, page, pageSize);
        }
        return PageDTO.map(pages);
    }

    @ResponseBody
    @GetMapping(path = "/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}", produces = {"application/json"})
    public ResultFeatureStatDTO getResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) throws ResultSetNotFoundException, ResultFeatureStatNotFoundException {
        return resultFeatureStatService.getResultFeatureStat(resultSetId, resultFeatureStatId);
    }

    @ResponseBody
    @PutMapping(path = "/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}", produces = {"application/json"})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HashMap<String, String> updateResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) {
        return new HashMap<>() {{
            put("status", "error");
            put("error", "ResultFeatureStat cannot be updated (it can be deleted).");
        }};
    }

    @ResponseBody
    @DeleteMapping("/resultset/{resultSetId}/resultfeaturestat/{resultFeatureStatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResultFeatureStat(@PathVariable long resultSetId, @PathVariable long resultFeatureStatId) throws ResultSetNotFoundException, InvalidResultSetIdException, ResultSetAlreadyCompletedException, ResultFeatureStatNotFoundException {
        resultFeatureStatService.delete(resultSetId, resultFeatureStatId);
    }

}
