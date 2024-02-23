/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
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
package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleException;

public class InvalidResultSetIdException extends UserVisibleException {

    private InvalidResultSetIdException(String msg) {
        super(msg);
    }

    public static InvalidResultSetIdException forResultData(long resultSetId, long resultDataId) {
        return new InvalidResultSetIdException(String.format("The ResultData with id %s is not owned by the ResultSet with id %s", resultDataId, resultSetId));
    }

    public static InvalidResultSetIdException forResultFeatureStat(long resultSetId, long resultDataId) {
        return new InvalidResultSetIdException(String.format("The ResultFeatureStat with id %s is not owned by the ResultSet with id %s", resultDataId, resultSetId));
    }

}
