package cz.tul.stin.backend.exception;

import cz.tul.stin.backend.model.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private String getLocalizedMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternalApiException(ExternalApiException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        log.error("External API error calling [{}]: {}", requestUrl, ex.getMessage(), ex);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                getLocalizedMessage("error.title.externalApi"),
                getLocalizedMessage(ex.getMessage())
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        log.warn("Resource not found (404) calling [{}]: {}", requestUrl, ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("error", getLocalizedMessage("error.title.notFound"));
        body.put("message", getLocalizedMessage("error.message.notFound"));
        body.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        log.error("Unexpected internal error (500) calling [{}]: {}", requestUrl, ex.getMessage(), ex);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getLocalizedMessage("error.title.internalServerError"),
                getLocalizedMessage("error.message.internalServerError")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        String url = request.getRequestURI();
        log.warn("Invalid input at [{}]: {}", url, ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                getLocalizedMessage("error.title.validation"),
                getLocalizedMessage(ex.getMessage()) // Např. klíč "error.date.invalid"
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(cz.tul.stin.backend.exception.StorageException.class)
    public ResponseEntity<ApiError> handleStorageException(cz.tul.stin.backend.exception.StorageException ex, HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        log.error("File system/storage error calling [{}]: {}", requestUrl, ex.getMessage(), ex);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getLocalizedMessage("error.title.storage"),
                getLocalizedMessage("error.message.storage")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        String url = request.getRequestURI();
        log.warn("Missing parameter at [{}]: {}", url, ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                getLocalizedMessage("error.title.missingParam"),
                getLocalizedMessage("error.message.missingParam", ex.getParameterName())
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}