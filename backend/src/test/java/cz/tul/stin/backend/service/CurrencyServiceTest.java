package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
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
        mockRates.put("GBP", null);

        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(mockRates);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        List<ExchangeRate> result = currencyService.getFilteredLatestRates("EUR");

        assertNotNull(result);
        assertEquals(2, result.size(), "List by měl obsahovat pouze 2 platné a nenulové měny");

        List<String> resultCurrencies = result.stream()
                .map(ExchangeRate::getCurrency)
                .toList();

        assertTrue(resultCurrencies.contains("CZK"));
        assertTrue(resultCurrencies.contains("USD"));
        assertFalse(resultCurrencies.contains("XYZ"), "Neplatná měna musí být odstraněna");
        assertFalse(resultCurrencies.contains("GBP"), "Měna s null hodnotou musí být odstraněna");

        ExchangeRate czkRate = result.stream().filter(r -> r.getCurrency().equals("CZK")).findFirst().get();
        assertEquals(25.0, czkRate.getRate());
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