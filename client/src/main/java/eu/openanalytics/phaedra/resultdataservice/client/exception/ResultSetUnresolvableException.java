package eu.openanalytics.phaedra.resultdataservice.client.exception;

public class ResultSetUnresolvableException extends Exception {
    public ResultSetUnresolvableException(String msg) {
        super(msg);
    }

    public ResultSetUnresolvableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
