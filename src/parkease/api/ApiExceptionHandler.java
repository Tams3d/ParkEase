package parkease.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import parkease.api.ApiDtos.ApiError;
import parkease.exception.DuplicateVehicleException;
import parkease.exception.InvalidHoursException;
import parkease.exception.InvalidOwnerException;
import parkease.exception.InvalidPlateException;
import parkease.exception.InvalidSlotException;
import parkease.exception.InvalidTicketException;
import parkease.exception.InvalidVehicleException;
import parkease.exception.ParkingFullException;
import parkease.exception.StorageException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler({
      InvalidPlateException.class,
      InvalidOwnerException.class,
      InvalidVehicleException.class,
      InvalidHoursException.class
  })
  public ResponseEntity<ApiError> badRequest(Exception error) {
    return response(HttpStatus.BAD_REQUEST, error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> unreadableBody() {
    return ResponseEntity.badRequest().body(new ApiError("Invalid request body."));
  }

  @ExceptionHandler({
      DuplicateVehicleException.class,
      ParkingFullException.class,
      InvalidSlotException.class
  })
  public ResponseEntity<ApiError> conflict(Exception error) {
    return response(HttpStatus.CONFLICT, error);
  }

  @ExceptionHandler(InvalidTicketException.class)
  public ResponseEntity<ApiError> notFound(InvalidTicketException error) {
    return response(HttpStatus.NOT_FOUND, error);
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ApiError> storage(StorageException error) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> unexpected(Exception error) {
    String message = error.getMessage();
    if (message == null || message.isBlank()) {
      message = "Unexpected server error.";
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError(message));
  }

  private ResponseEntity<ApiError> response(HttpStatus status, Exception error) {
    String message = error.getMessage();
    if (message == null || message.isBlank()) {
      message = "Request failed.";
    }
    return ResponseEntity.status(status).body(new ApiError(message));
  }
}
