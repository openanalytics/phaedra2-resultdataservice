package eu.openanalytics.phaedra.resultdataservice.exception;

public class InvalidResultSetIdException extends UserVisibleException {

    public InvalidResultSetIdException(long resultSetId, long resultDataId) {
        super(String.format("The ResultData with id %s is not owned by the ResultSet with id %s", resultDataId, resultSetId));
    }
}
