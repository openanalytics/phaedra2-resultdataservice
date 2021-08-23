package eu.openanalytics.phaedra.phaedra2resultdataservice.exception;

public class ResultDataSetAlreadyCompletedException extends UserVisibleException {

    public ResultDataSetAlreadyCompletedException() {
        super("ResultDataSet already contains a complete message or end timestamp.");
    }

}
