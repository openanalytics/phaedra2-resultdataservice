package eu.openanalytics.phaedra.resultdataservice.exception;

public class ResultFeatureStatNotFoundException extends EntityNotFoundException {

    public ResultFeatureStatNotFoundException(long resultDataId) {
        super(String.format("ResultFeatureStat with id %s not found!", resultDataId));
    }

}
