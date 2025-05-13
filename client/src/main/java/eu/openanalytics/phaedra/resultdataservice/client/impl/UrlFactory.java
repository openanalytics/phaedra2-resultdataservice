/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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

	private static final int PAGE_SIZE = 100;
	
    private String baseURL;
    
    public UrlFactory(String baseURL) {
    	this.baseURL = baseURL;
	}

    public String resultSet() {
        return String.format("%s/resultsets", baseURL);
    }

    public String resultSet(long resultId) {
        return String.format("%s/resultsets/%s", baseURL, resultId);
    }

    public String resultSet(StatusCode outcome, int page) {
        return String.format("%s/resultsets?outcome=%s&page=%s&pageSize=%s", baseURL, outcome, page, PAGE_SIZE);
    }

    public String latestResultSetByPlateId(long plateId) {
        return String.format("%s/resultsets/latest?plateId=%s", baseURL, plateId);
    }

    public String latestResultSetByPlateIdAndMeasId(long plateId, long measId) {
        return String.format("%s/resultsets/latest?plateId=%s&measId=%s&n=%s", baseURL, plateId, measId, 1);
    }

    public String latestResultSetByPlateIdAndProtocolId(long plateId, long protocolId) {
        return String.format("%s/resultsets/latest?plateId=%s&protocolId=%s&n=%s", baseURL, plateId, protocolId, 1);
    }

    public String latestPlateResults(long plateId) {
        return String.format("%s/plate-results/%s/latest", baseURL, plateId);
    }

    public String latestPlateResultsByMeasId(long plateId, long measId) {
        return String.format("%s/plate-results/%s/latest?measId=%s", baseURL, plateId, measId);
    }

    public String latestPlateResultsAndProtocolId(long plateId, long protocolId) {
        return String.format("%s/plate-results/%s/latest?protocolId=%s", baseURL, plateId, protocolId);
    }

    public String resultData(long resultSetId) {
        return String.format("%s/resultsets/%s/resultdata", baseURL, resultSetId);
    }

    public String resultData(long resultSetId, int page) {
        return String.format("%s/resultsets/%s/resultdata?page=%s&pageSize=%s", baseURL, resultSetId, page, PAGE_SIZE);
    }

    public String resultDataByFeatureId(long resultSetId, long featureId) {
        return String.format("%s/resultsets/%s/resultdata?featureId=%s", baseURL, resultSetId, featureId);
    }

    public String resultFeatureStat(long resultSetId) {
        return String.format("%s/resultsets/%s/resultfeaturestats", baseURL, resultSetId);
    }

    public String resultFeatureStat(long resultSetId, int currentPage) {
        return String.format("%s/resultsets/%s/resultfeaturestats?page=%s&pageSize=%s", baseURL, resultSetId, currentPage, PAGE_SIZE);
    }

    public String resultFeatureStatByFeatureStatId(long resultSetId, long featureStatId) {
        return String.format("%s/resultsets/%s/resultfeaturestats/%s", baseURL, resultSetId, featureStatId);
    }
}
