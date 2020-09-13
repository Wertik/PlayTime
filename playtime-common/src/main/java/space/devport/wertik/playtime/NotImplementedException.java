package space.devport.wertik.playtime;

/**
 * Replacement for org.apache exception.
 */
public class NotImplementedException extends RuntimeException {
    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException() {
        super();
    }
}