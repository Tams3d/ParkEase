package parkease.exception;

public class StorageException extends Exception {

  public StorageException(String message) {
    super(message);
  }

  public StorageException(String message, Exception cause) {
    super(message, cause);
  }
}
