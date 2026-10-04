package com.stayo.stayo.shared.exception;

import com.stayo.stayo.shared.dto.ApiError;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");

    @Test
    void springWebExceptionsKeepTheirOwnStatus() {
        assertEquals(404, handler.handleGeneric(new NoResourceFoundException(HttpMethod.GET, "/api/x", "api/x"), request).getStatusCode().value());
        ResponseEntity<ApiError> notAllowed = handler.handleGeneric(new HttpRequestMethodNotSupportedException("DELETE", java.util.List.of("GET")), request);
        assertEquals(405, notAllowed.getStatusCode().value());
        assertEquals("GET", notAllowed.getHeaders().getFirst("Allow"));
    }

    @Test
    void unexpectedErrorIs500WithoutLeakingInternals() {
        ResponseEntity<ApiError> response = handler.handleGeneric(new IllegalStateException("secret internal detail"), request);
        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().getMessage().contains("secret"));
    }

    @Test
    void uploadTooLargeIs413AndDatabaseOutageIs503() {
        assertEquals(413, handler.handleMaxUploadSize(new MaxUploadSizeExceededException(8_000_000), request).getStatusCode().value());
        assertEquals(503, handler.handleDataAccess(new DataAccessResourceFailureException("mongo down"), request).getStatusCode().value());
    }
}
