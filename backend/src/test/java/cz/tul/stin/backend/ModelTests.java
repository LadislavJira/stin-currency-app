package cz.tul.stin.backend;

import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTests {

    @Test
    void testCurrencyEnum() {
        assertTrue(CurrencySymbol.isValid("CZK"));
        assertTrue(CurrencySymbol.isValid("USD"));
        assertFalse(CurrencySymbol.isValid("NEEXISTUJE"));
    }

    @Test
    void testLombokDTO() {
        LatestRatesResponse response = new LatestRatesResponse();
        response.setBase("EUR");
        response.setSuccess(true);

        assertEquals("EUR", response.getBase());
        assertTrue(response.isSuccess());
    }
}