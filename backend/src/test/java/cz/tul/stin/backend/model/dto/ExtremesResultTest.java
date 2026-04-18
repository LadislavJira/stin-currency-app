package cz.tul.stin.backend.model.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExtremesResultTest {

    @Test
    void testGettersAndSetters() {
        ExtremesResult result = new ExtremesResult("CZK", 25.0, "USD", 1.1);

        assertEquals("CZK", result.getStrongestCurrency());
        assertEquals(25.0, result.getStrongestValue());
        assertEquals("USD", result.getWeakestCurrency());
        assertEquals(1.1, result.getWeakestValue());

        result.setStrongestCurrency("EUR");
        assertEquals("EUR", result.getStrongestCurrency());
    }
}