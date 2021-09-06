package eu.openanalytics.phaedra.resultdataservice.exception;

abstract public class EntityNotFoundException extends UserVisibleException {

    public EntityNotFoundException(String msg) {
        super(msg);
    }

}
