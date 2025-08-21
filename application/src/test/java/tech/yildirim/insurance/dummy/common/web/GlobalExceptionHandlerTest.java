package tech.yildirim.insurance.dummy.common.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.yildirim.insurance.dummy.common.ResourceNotFoundException;

/**
 * Tests for the {@link GlobalExceptionHandler}. We use a dummy controller defined inside this test
 * class to trigger exceptions.
 */
@WebMvcTest
@ContextConfiguration(
    classes = {GlobalExceptionHandler.class, GlobalExceptionHandlerTest.DummyController.class})
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Data
  static class DummyDto {
    @NotBlank(message = "Name cannot be blank")
    private String name;
  }

  @RestController
  static class DummyController {
    @PostMapping("/test/validation")
    public ResponseEntity<Void> testValidation(@Valid @RequestBody DummyDto dto) {
      return ResponseEntity.ok().build();
    }

    @GetMapping("/test/not-found")
    public ResponseEntity<Void> testNotFound() {
      throw new ResourceNotFoundException("The requested dummy resource was not found");
    }
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException and return 400 with structured error")
  void whenValidationFails_shouldReturn400BadRequest() throws Exception {
    // Given: A DTO with a blank name, which violates the validation rule
    DummyDto invalidDto = new DummyDto();
    invalidDto.setName("");

    // When & Then: Perform a POST request and check for the structured error response
    mockMvc
        .perform(
            post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")))
        .andExpect(jsonPath("$.message", is("Validation failed for requests.")))
        .andExpect(jsonPath("$.details.name", is("Name cannot be blank")));
  }

  @Test
  @DisplayName("Should handle ResourceNotFoundException and return 404 with structured error")
  void whenResourceNotFound_shouldReturn404NotFound() throws Exception {
    // When & Then: Perform a GET request to the endpoint that throws ResourceNotFoundException
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status", is(404)))
        .andExpect(jsonPath("$.error", is("Not Found")))
        .andExpect(jsonPath("$.message", is("The requested dummy resource was not found")))
        .andExpect(jsonPath("$.timestamp", notNullValue()));
  }
}
