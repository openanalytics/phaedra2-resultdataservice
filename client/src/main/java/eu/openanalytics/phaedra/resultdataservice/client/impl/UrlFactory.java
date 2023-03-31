/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.client.impl;

import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;

public class UrlFactory {

    private static final String RESULTDATA_SERVICE = "http://phaedra-resultdata-service:8080/phaedra/resultdata-service";
    private static final int PAGE_SIZE = 100;

    public static String resultSet() {
        return String.format("%s/resultsets", RESULTDATA_SERVICE);
    }

    public static String resultSet(long resultId) {
        return String.format("%s/resultsets/%s", RESULTDATA_SERVICE, resultId);
    }

    public static String resultSet(StatusCode outcome, int page) {
        return String.format("%s/resultsets?outcome=%s&page=%s&pageSize=%s", RESULTDATA_SERVICE, outcome, page, PAGE_SIZE);
    }

    public static String resultSetLatest(long plateId, long measId) {
        return String.format("%s/resultsets/latest?plateId=%s&measId=%s&n=%s", RESULTDATA_SERVICE, plateId, measId, 1);
    }

    public static String resultData(long resultSetId) {
        return String.format("%s/resultsets/%s/resultdata", RESULTDATA_SERVICE, resultSetId);
    }

    public static String resultData(long resultSetId, int page) {
        return String.format("%s/resultsets/%s/resultdata?page=%s&pageSize=%s", RESULTDATA_SERVICE, resultSetId, page, PAGE_SIZE);
    }

    public static String resultDataByFeatureId(long resultSetId, long featureId) {
        return String.format("%s/resultsets/%s/resultdata?featureId=%s", RESULTDATA_SERVICE, resultSetId, featureId);
    }

    public static String resultFeatureStat(long resultSetId) {
        return String.format("%s/resultsets/%s/resultfeaturestats", RESULTDATA_SERVICE, resultSetId);
    }

    public static String resultFeatureStat(long resultSetId, int currentPage) {
        return String.format("%s/resultsets/%s/resultfeaturestats?page=%s&pageSize=%s", RESULTDATA_SERVICE, resultSetId, currentPage, PAGE_SIZE);
    }

    public static String resultFeatureStatByFeatureStatId(long resultSetId, long featureStatId) {
        return String.format("%s/resultsets/%s/resultfeaturestats/%s", RESULTDATA_SERVICE, resultSetId, featureStatId);
    }



}
