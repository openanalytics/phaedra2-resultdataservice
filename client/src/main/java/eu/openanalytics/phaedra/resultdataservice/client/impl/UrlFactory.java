package eu.openanalytics.phaedra.resultdataservice.client.impl;

public class UrlFactory {

    private static final String RESULTDATA_SERVICE = "http://phaedra-resultdata-service/phaedra/resultdata-service";
    private static final int PAGE_SIZE = 100;

    public static String resultSet() {
        return String.format("%s/resultset", RESULTDATA_SERVICE);
    }

    public static String resultSet(long resultId) {
        return String.format("%s/resultset/%s", RESULTDATA_SERVICE, resultId);
    }

    public static String resultData(long resultSetId) {
        return String.format("%s/resultset/%s/resultdata", RESULTDATA_SERVICE, resultSetId);
    }

    public static String resultData(long resultSetId, int page) {
        return String.format("%s/resultset/%s/resultdata?page=%s&pageSize=%s", RESULTDATA_SERVICE, resultSetId, page, PAGE_SIZE);
    }

    public static String resultDataByFeatureId(long resultSetId, long featureId) {
        return String.format("%s/resultset/%s/resultdata?featureId=%s", RESULTDATA_SERVICE, resultSetId, featureId);
    }

    public static String resultFeatureStat(long resultSetId) {
        return String.format("%s/resultset/%s/resultfeaturestat", RESULTDATA_SERVICE, resultSetId);
    }

    public static String resultFeatureStat(long resultSetId, int currentPage) {
        return String.format("%s/resultset/%s/resultfeaturestat?page=%s&pageSize=%s", RESULTDATA_SERVICE, resultSetId, currentPage, PAGE_SIZE);
    }

    public static String resultFeatureStatByFeatureStatId(long resultSetId, long featureStatId) {
        return String.format("%s/resultset/%s/resultfeaturestat/%s", RESULTDATA_SERVICE, resultSetId, featureStatId);
    }

}
