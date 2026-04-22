package cz.tul.stin.backend.exception;

import cz.tul.stin.backend.model.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
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
        assertEquals("Chyba externího API", response.getBody().getError());
        assertEquals("Služba exchangerate neodpovídá", response.getBody().getMessage());
    }

    @Test
    void handleNotFound_ShouldReturn404AndMapBody() {
        when(request.getRequestURI()).thenReturn("/api/neexistuje");
        when(request.getQueryString()).thenReturn(null);

        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/neexistuje", "api/neexistuje");

        // AKCE
        ResponseEntity<Object> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(404, body.get("status"));
        assertEquals("Nenalezeno", body.get("error"));
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
        assertEquals("Interní chyba serveru", response.getBody().getError());
        assertEquals("Došlo k neočekávané chybě. Zkuste to prosím později.", response.getBody().getMessage());

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

        String errorMessage = "Neplatný kód základní měny: ZEME";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        ResponseEntity<ApiError> response = handler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(400, response.getBody().getStatus());
        assertEquals("Chyba validace dat", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

}