package parkease.exception;

public class ApiException extends Exception {

  public ApiException(String message) {
    super(message);
  }

  public ApiException(String message, Exception cause) {
    super(message, cause);
  }
}
