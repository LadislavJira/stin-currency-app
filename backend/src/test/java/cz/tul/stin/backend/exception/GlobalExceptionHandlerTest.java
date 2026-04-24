package cz.tul.stin.backend.exception;

import cz.tul.stin.backend.model.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);

        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return key;
        });

        handler = new GlobalExceptionHandler(messageSource);
        request = mock(HttpServletRequest.class);
    }

    @Test
    void handleExternalApiException_ShouldReturn400AndKeepOriginalMessage() {
        when(request.getRequestURI()).thenReturn("/api/currencies/latest");
        when(request.getQueryString()).thenReturn("symbols=CZK");

        ExternalApiException ex = new ExternalApiException("Služba exchangerate neodpovídá");

        ResponseEntity<ApiError> response = handler.handleExternalApiException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(400, response.getBody().getStatus());
        assertEquals("error.title.externalApi", response.getBody().getError());
        assertEquals("Služba exchangerate neodpovídá", response.getBody().getMessage());
    }

    @Test
    void handleNotFound_ShouldReturn404AndMapBody() {
        when(request.getRequestURI()).thenReturn("/api/neexistuje");
        when(request.getQueryString()).thenReturn(null);

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/neexistuje", "api/neexistuje");

        ResponseEntity<Object> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(404, body.get("status"));
        assertEquals("error.title.notFound", body.get("error"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleGeneralException_ShouldReturn500AndHIDEOriginalMessage() {
        when(request.getRequestURI()).thenReturn("/api/settings");
        when(request.getQueryString()).thenReturn(null);

        String secretServerDetails = "FATAL: Connection to jdbc:mysql://localhost:3306 with password 'TajneHeslo123' failed";
        Exception ex = new Exception(secretServerDetails);

        ResponseEntity<ApiError> response = handler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());

        assertEquals("error.title.internalServerError", response.getBody().getError());
        assertEquals("error.message.internalServerError", response.getBody().getMessage());

        assertFalse(response.getBody().getMessage().contains("TajneHeslo123"));
    }

    @Test
    void handleExternalApiException_ShouldHandleNullQueryString() {
        when(request.getRequestURI()).thenReturn("/api/currencies/latest");
        when(request.getQueryString()).thenReturn(null);

        ExternalApiException ex = new ExternalApiException("Testovací chyba");
        ResponseEntity<ApiError> response = handler.handleExternalApiException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleNotFound_ShouldHandleNonNullQueryString() {
        when(request.getRequestURI()).thenReturn("/api/neexistuje");
        when(request.getQueryString()).thenReturn("param=123");

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/neexistuje", "api/neexistuje");
        ResponseEntity<Object> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleGeneralException_ShouldHandleNonNullQueryString() {
        when(request.getRequestURI()).thenReturn("/api/settings");
        when(request.getQueryString()).thenReturn("debug=true");

        Exception ex = new Exception("Test");
        ResponseEntity<ApiError> response = handler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400AndValidationErrorMessage() {
        when(request.getRequestURI()).thenReturn("/api/settings");

        String errorMessage = "error.date.invalid";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        ResponseEntity<ApiError> response = handler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(400, response.getBody().getStatus());
        assertEquals("error.title.validation", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleStorageException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        StorageException ex = new StorageException("Disk error", new RuntimeException());

        ResponseEntity<ApiError> response = handler.handleStorageException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error.title.storage", response.getBody().getError());
    }

    @Test
    void testHandleMissingParams() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("symbols", "String");

        ResponseEntity<ApiError> response = handler.handleMissingParams(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error.title.missingParam", response.getBody().getError());
    }
}