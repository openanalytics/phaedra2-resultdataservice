package eu.openanalytics.phaedra.resultdataservice.exception;

public class ResultSetNotFoundException extends EntityNotFoundException {

    public ResultSetNotFoundException(long resultSetId) {
        super(String.format("ResultSet with id %s not found!", resultSetId));
    }

}

