package eu.openanalytics.phaedra.phaedra2resultdataservice.exception;

public class ResultDataSetNotFoundException extends UserVisibleException {

    public ResultDataSetNotFoundException(long resultDataSetId) {
        super(String.format("ResultDataSet with id %s not found!", resultDataSetId));
    }

}

