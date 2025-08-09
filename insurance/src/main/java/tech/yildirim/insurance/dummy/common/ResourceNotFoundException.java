package tech.yildirim.insurance.dummy.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to be thrown when a requested resource is not found in the system. Annotated
 * with @ResponseStatus(HttpStatus.NOT_FOUND) so that Spring automatically translates this exception
 * into a 404 Not Found HTTP response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
