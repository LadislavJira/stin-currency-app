package cz.tul.stin.backend.model; // Uprav balíček podle toho, kde Enum skutečně leží

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencySymbolTest {

    @Test
    void testIsValid_WithNullCode() {
        assertFalse(CurrencySymbol.isValid(null), "Null kód by měl vrátit false");
    }

    @Test
    void testIsValid_WithValidCode() {
        assertTrue(CurrencySymbol.isValid("EUR"));
        assertTrue(CurrencySymbol.isValid("eur"));
    }

    @Test
    void testIsValid_WithInvalidCode() {
        assertFalse(CurrencySymbol.isValid("XYZ"));
    }
}