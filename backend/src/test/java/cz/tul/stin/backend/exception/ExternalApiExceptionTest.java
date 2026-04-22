package cz.tul.stin.backend.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExternalApiExceptionTest {

    @Test
    void testExceptionMessage() {
        String errorMessage = "Chyba komunikace s burzou";
        ExternalApiException exception = new ExternalApiException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }
}