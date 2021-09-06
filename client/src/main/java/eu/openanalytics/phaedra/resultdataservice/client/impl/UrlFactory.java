package eu.openanalytics.phaedra.resultdataservice.client.impl;

public class UrlFactory {

    private static final String RESULTDATA_SERVICE = "http://phaedra-resultdata-service/phaedra/resultdata-service";

    public static String resultSet() {
        return String.format("%s/resultset", RESULTDATA_SERVICE);
    }

    public static String resultSet(long resultId) {
        return String.format("%s/resultset/%s", RESULTDATA_SERVICE, resultId);
    }

    public static String resultData(long resultSetId) {
        return String.format("%s/resultset/%s/resultdata", RESULTDATA_SERVICE, resultSetId);
    }

    public static String resultDataByFeatureId(long resultSetId, long featureId) {
        return String.format("%s/resultset/%s/resultdata?featureId=%s", RESULTDATA_SERVICE, resultSetId, featureId);
    }
}
