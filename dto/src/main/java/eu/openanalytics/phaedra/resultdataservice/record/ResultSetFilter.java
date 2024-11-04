package eu.openanalytics.phaedra.resultdataservice.record;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import java.util.List;

public record ResultSetFilter(
    List<Long> ids,
    List<Long> plateIds,
    List<Long> measurementIds,
    List<Long> protocolIds,
    List<StatusCode> status
) {

}
