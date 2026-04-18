package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void testGetFilteredLatestRates_Success() {
        Map<String, Double> mockRates = new HashMap<>();
        mockRates.put("CZK", 25.0);
        mockRates.put("USD", 1.1);
        mockRates.put("XYZ", 100.0);

        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(mockRates);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        Map<String, Double> result = currencyService.getFilteredLatestRates("EUR");

        assertNotNull(result);
        assertEquals(2, result.size(), "Mapa by měla obsahovat pouze 2 platné měny");
        assertTrue(result.containsKey("CZK"));
        assertTrue(result.containsKey("USD"));
        assertFalse(result.containsKey("XYZ"), "Neplatná měna musí být odstraněna");
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException() {
        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            currencyService.getFilteredLatestRates("EUR");
        });

        assertTrue(exception.getMessage().contains("Nepodařilo se získat data"));
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException_WhenSuccessIsFalse() {
        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(false);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> {
            currencyService.getFilteredLatestRates("EUR");
        });
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException_WhenRatesAreNull() {
        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(null);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> {
            currencyService.getFilteredLatestRates("EUR");
        });
    }
}