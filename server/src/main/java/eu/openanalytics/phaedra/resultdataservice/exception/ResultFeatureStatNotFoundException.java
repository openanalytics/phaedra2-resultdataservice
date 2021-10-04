package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.EntityNotFoundException;

public class ResultFeatureStatNotFoundException extends EntityNotFoundException {

    public ResultFeatureStatNotFoundException(long resultDataId) {
        super(String.format("ResultFeatureStat with id %s not found!", resultDataId));
    }

}
