package eu.openanalytics.phaedra.resultdataservice.record;

import java.util.List;

public record ResultDataFilter(
    List<String> ids,
    List<String> resultDataIds,
    List<String> resultSetIds,
    List<String> protocolIds,
    List<String> featureIds
) {
  public ResultDataFilter {
    // Ensure the lists are never null
    ids = (ids != null) ? ids : List.of();
    resultDataIds = (resultDataIds != null) ? resultDataIds : List.of();
    resultSetIds = (resultSetIds != null) ? resultSetIds : List.of();
    protocolIds = (protocolIds != null) ? protocolIds : List.of();
    featureIds = (featureIds != null) ? featureIds : List.of();
  }
}
