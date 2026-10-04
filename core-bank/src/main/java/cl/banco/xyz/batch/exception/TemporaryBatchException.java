package cl.banco.xyz.batch.exception;

public class TemporaryBatchException extends RuntimeException {
    public TemporaryBatchException(String message) {
        super(message);
    }
}