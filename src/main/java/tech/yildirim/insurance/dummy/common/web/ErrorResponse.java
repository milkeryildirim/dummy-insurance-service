package tech.yildirim.insurance.dummy.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A standardized error response structure for the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private ZonedDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private Map<String, String> details;
}
