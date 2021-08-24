package eu.openanalytics.phaedra.phaedra2resultdataservice.exception;

abstract public class EntityNotFoundException extends UserVisibleException {

    public EntityNotFoundException(String msg) {
        super(msg);
    }

}
