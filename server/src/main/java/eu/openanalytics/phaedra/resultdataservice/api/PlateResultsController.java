package eu.openanalytics.phaedra.resultdataservice.api;

import eu.openanalytics.phaedra.resultdataservice.dto.PlateResultDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.service.ModelMapper;
import eu.openanalytics.phaedra.resultdataservice.service.ResultDataService;
import eu.openanalytics.phaedra.resultdataservice.service.ResultFeatureStatService;
import eu.openanalytics.phaedra.resultdataservice.service.ResultSetService;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleException;
import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleExceptionHandler;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//@CrossOrigin
@RestController
@Validated
public class PlateResultsController implements UserVisibleExceptionHandler {

    private final ResultSetService resultSetService;
    private final ResultDataService resultDataService;
    private final ResultFeatureStatService resultFeatureStatService;
    private final ModelMapper modelMapper;

    public PlateResultsController(ResultSetService resultSetService, ResultDataService resultDataService, ResultFeatureStatService resultFeatureStatService, ModelMapper modelMapper) {
        this.resultSetService = resultSetService;
        this.resultDataService = resultDataService;
        this.resultFeatureStatService = resultFeatureStatService;
        this.modelMapper = modelMapper;
    }

    @ResponseBody
    @GetMapping(path = "/plate-results/{plateId}", produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public PlateResultDTO getPlateResults(@PathVariable(name = "plateId") Long plateId,
                                          @RequestParam(name = "measId") Optional<Long> measId) throws UserVisibleException {

        var resultSets = resultSetService.getResultSetsByPlateId(plateId, measId);
        return getPlateResults(resultSets);
    }

    @ResponseBody
    @GetMapping(path = "/plate-results/{plateId}/latest", produces = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public PlateResultDTO getLatestPlateResults(@PathVariable(name = "plateId") Long plateId,
                                                @RequestParam(name = "measId") Optional<Long> measId) throws UserVisibleException {
        var resultSets = resultSetService.getLatestResultSetsByPlateId(plateId, measId);
        return getPlateResults(resultSets);
    }

    private PlateResultDTO getPlateResults(List<ResultSetDTO> resultSets) throws UserVisibleException {
        if (resultSets.size() > 100) {
            throw new UserVisibleException(String.format("Found too many ResultSets for this plate, only 100 ResultSets can be prossed, found %s ResultSets.", resultSets.size()));
        }

        var resultIds = resultSets.stream().map(ResultSetDTO::getId).toList();

        // 2. get ResultData for these ResultSets and group by ResultData
        var resultData = resultDataService
            .getResultDataByResultSetIds(resultIds)
            .stream()
            .collect(Collectors.groupingBy(
                ResultData::getResultSetId,
                Collectors.toList()
            ));

        // 3. get ResultFeatureStats for these ResultData and group by ResultSetId and FeatureId (FeatureId is unique for one protocol)
        var resultFeatureStats = resultFeatureStatService.getResultFeatureStatsByResultIds(resultIds)
            .stream()
            .collect(Collectors.groupingBy(
                ResultFeatureStatDTO::getResultSetId,
                Collectors.groupingBy(
                    ResultFeatureStatDTO::getFeatureId,
                    Collectors.toList()
                )
            ));

        // 4. group resultSets by ProtocolId and MeasId
        var resultSetsByProtocolAndMeasId = resultSets.stream()
            .collect(Collectors.groupingBy(
                    ResultSetDTO::getProtocolId,
                    Collectors.groupingBy(
                        ResultSetDTO::getMeasId,
                        Collectors.toList()
                    )
                )
            );

        // convert all the data
        var results = new HashMap<Long, PlateResultDTO.ResultsPerProtocolDTO>();
        resultSetsByProtocolAndMeasId.forEach((protocolId, measurements) -> {
            var protocolBuilder = PlateResultDTO.ResultsPerProtocolDTO.builder();

            // convert the measurements into DTO objects
            var convertedMeasurements = new ArrayListValuedHashMap<Long, PlateResultDTO.ResultsPerMeasurement>();
            measurements.forEach((measurementId, measurement) -> {
                for (var resultSet : measurement) {
                    if (!resultData.containsKey(resultSet.getId())) continue;
                    // for each ResultSet, get ResultData, convert it to DTO and attach the relevant FeatureStats
                    var convertedResultData = resultData.get(resultSet.getId())
                        .stream()
                        .map(it -> modelMapper.map(it)
                                .resultFeatureStats(
                                    getFeatureStats(resultFeatureStats, it)
                                ).build()
                        ).toList();

                    convertedMeasurements.put(measurementId, PlateResultDTO.ResultsPerMeasurement.builder().resultData(convertedResultData).build());
                }
            });

            protocolBuilder.measurements(convertedMeasurements.asMap());

            results.put(protocolId, protocolBuilder.build());
        });

        return PlateResultDTO.builder().protocols(results).build();
    }

    private List<ResultFeatureStatDTO> getFeatureStats(Map<Long, Map<Long, List<ResultFeatureStatDTO>>> resultFeatureStats, ResultData resultData) {
        if (resultFeatureStats.containsKey(resultData.getResultSetId())) {
            if (resultFeatureStats.get(resultData.getResultSetId()).containsKey(resultData.getFeatureId())) {
                return resultFeatureStats.get(resultData.getResultSetId()).get(resultData.getFeatureId());
            }
        }
        return Collections.emptyList();
    }

}
