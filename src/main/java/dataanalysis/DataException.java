package dataanalysis;

/**
 * General data loading exception
 */
public class DataException extends RuntimeException {
    public DataException(Throwable t) {
        super(t);
    }

    public DataException(String message) {
        super(message);
    }
}
