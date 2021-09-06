package eu.openanalytics.phaedra.resultdataservice.exception;

public class ResultDataNotFoundException extends EntityNotFoundException {

    public ResultDataNotFoundException(long resultDataId) {
        super(String.format("ResultData with id %s not found!", resultDataId));
    }

}
