package eu.openanalytics.phaedra.resultdataservice.client.exception;

public class ResultFeatureStatUnresolvableException extends Exception {
    public ResultFeatureStatUnresolvableException(String msg) {
        super(msg);
    }

    public ResultFeatureStatUnresolvableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
