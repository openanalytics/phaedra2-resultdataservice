package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.EntityNotFoundException;

public class ResultSetNotFoundException extends EntityNotFoundException {

    public ResultSetNotFoundException(long resultSetId) {
        super(String.format("ResultSet with id %s not found!", resultSetId));
    }

}

