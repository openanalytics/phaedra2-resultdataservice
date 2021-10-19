package eu.openanalytics.phaedra.resultdataservice.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class PlateResultDTO {

    Map<Long, ResultsPerProtocolDTO> protocols;

    @Value
    @Builder
    public static class ResultsPerProtocolDTO {
        Map<Long, Collection<ResultsPerMeasurement>> measurements;
    }

    @Value
    @Builder
    public static class ResultsPerMeasurement {
        List<ResultDataDTO> resultData;
    }

}


