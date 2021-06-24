package nl.utwente.m4.lossprevention.InputSanitization;

public class InputNotAllowedException extends Exception{
    public InputNotAllowedException() {
        super();
    }

    public InputNotAllowedException(String message) {
        super(message);
    }
}
