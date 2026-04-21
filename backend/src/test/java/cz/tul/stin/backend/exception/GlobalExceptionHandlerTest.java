package cz.tul.stin.backend.exception;

import cz.tul.stin.backend.model.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleExternalApiException() {
        ExternalApiException ex = new ExternalApiException("Konkrétní detail chyby pro uživatele");

        ResponseEntity<ApiError> response = handler.handleExternalApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Chyba externího API", response.getBody().getError());
        assertEquals("Konkrétní detail chyby pro uživatele", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new Exception("Tajná chyba databáze SQL - nesmí se ukázat uživateli!");

        ResponseEntity<ApiError> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Interní chyba serveru", response.getBody().getError());
        assertEquals("Došlo k neočekávané chybě. Zkuste to prosím později.", response.getBody().getMessage());

        assertFalse(response.getBody().getMessage().contains("Tajná chyba"));
    }
}