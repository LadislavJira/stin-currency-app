package cz.tul.stin.backend.service;

import cz.tul.stin.backend.model.CurrencySymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyServiceTest {

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService();
    }

    @Test
    void testGetAvailableSymbols_ReturnsAllEnumValuesAsStrings() {
        List<String> symbols = currencyService.getAvailableSymbols();

        assertNotNull(symbols, "Seznam měn nesmí být null.");
        assertFalse(symbols.isEmpty(), "Seznam měn nesmí být prázdný.");

        int expectedSize = CurrencySymbol.values().length;
        assertEquals(expectedSize, symbols.size(), "Velikost vráceného seznamu musí přesně odpovídat počtu prvků v Enumu.");

        assertTrue(symbols.contains("CZK"), "Seznam musí obsahovat českou korunu (CZK).");
        assertTrue(symbols.contains("EUR"), "Seznam musí obsahovat euro (EUR).");
        assertTrue(symbols.contains("USD"), "Seznam musí obsahovat americký dolar (USD).");
        assertTrue(symbols.contains("JPY"), "Seznam musí obsahovat japonský jen (JPY).");
    }
}