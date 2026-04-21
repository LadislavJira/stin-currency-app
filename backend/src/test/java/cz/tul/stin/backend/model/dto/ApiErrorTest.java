package cz.tul.stin.backend.model.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void testApiErrorConstructorAndGetters() {
        ApiError error = new ApiError(404, "Not Found", "Item does not exist");

        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals("Item does not exist", error.getMessage());
    }

    @Test
    void testApiErrorSettersAndLombokMethods() {
        ApiError error1 = new ApiError(400, "Bad Request", "Message 1");

        error1.setStatus(500);
        error1.setError("Server Error");
        error1.setMessage("Message 2");
        LocalDateTime now = LocalDateTime.now();
        error1.setTimestamp(now);

        assertEquals(500, error1.getStatus());
        assertEquals("Server Error", error1.getError());
        assertEquals("Message 2", error1.getMessage());
        assertEquals(now, error1.getTimestamp());

        ApiError error2 = new ApiError(500, "Server Error", "Message 2");
        error2.setTimestamp(now);

        assertEquals(error1, error2);
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotNull(error1.toString());
    }
}