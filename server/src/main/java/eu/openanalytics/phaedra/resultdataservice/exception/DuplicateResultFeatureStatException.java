package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.UserVisibleException;

public class DuplicateResultFeatureStatException extends UserVisibleException {

    public DuplicateResultFeatureStatException() {
        // TODO be more specific
        super("ResultFeatureStat with one of these parameters already exists!");

    }
}
