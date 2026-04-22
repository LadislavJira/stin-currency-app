package cz.tul.stin.backend.exception;

import cz.tul.stin.backend.model.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternalApiException(ExternalApiException ex, HttpServletRequest request) {

        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        log.error("Chyba externího API při volání [{}]: {}", requestUrl, ex.getMessage(), ex);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Chyba externího API",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        log.warn("Nenalezeno (404) při volání [{}]: {}", requestUrl, ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("error", "Nenalezeno");
        body.put("message", "Tato adresa na serveru neexistuje. Zkontrolujte URL.");
        body.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

       log.error("Neočekávaná interní chyba (500) při volání [{}]: {}", requestUrl, ex.getMessage(), ex);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Interní chyba serveru",
                "Došlo k neočekávané chybě. Zkuste to prosím později."
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}