package eu.openanalytics.phaedra.phaedra2resultdataservice.exception;

public class ResultDataNotFoundException extends EntityNotFoundException {

    public ResultDataNotFoundException(long resultDataId) {
        super(String.format("ResultData with id %s not found!", resultDataId));
    }

}
