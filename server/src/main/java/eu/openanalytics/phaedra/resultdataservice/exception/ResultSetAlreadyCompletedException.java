package eu.openanalytics.phaedra.resultdataservice.exception;

public class ResultSetAlreadyCompletedException extends UserVisibleException {

    public ResultSetAlreadyCompletedException() {
        super("ResultDataSet already contains a complete message or end timestamp.");
    }

    public ResultSetAlreadyCompletedException(String msg) {
        super(msg);
    }

}
