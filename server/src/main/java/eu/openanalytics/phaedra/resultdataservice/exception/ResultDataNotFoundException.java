package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.EntityNotFoundException;

public class ResultDataNotFoundException extends EntityNotFoundException {

    public ResultDataNotFoundException(long resultDataId) {
        super(String.format("ResultData with id %s not found!", resultDataId));
    }

}
