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
