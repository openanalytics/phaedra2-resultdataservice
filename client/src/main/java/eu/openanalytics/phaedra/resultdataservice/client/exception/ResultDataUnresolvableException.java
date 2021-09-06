package eu.openanalytics.phaedra.resultdataservice.client.exception;

public class ResultDataUnresolvableException extends Exception {
    public ResultDataUnresolvableException(String msg) {
        super(msg);
    }

    public ResultDataUnresolvableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
