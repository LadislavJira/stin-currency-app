package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.LiveRatesResponse;
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
        Map<String, Double> mockQuotes = new HashMap<>();
        mockQuotes.put("EURCZK", 25.0);
        mockQuotes.put("EURUSD", 1.1);

        mockQuotes.put("EURXYZ", 100.0);
        mockQuotes.put("EURGBP", null);
        mockQuotes.put("SHORT", 5.0);
        mockQuotes.put(null, 10.0);

        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(mockQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK,USD"))
                .thenReturn(mockResponse);

        List<ExchangeRate> result = currencyService.getFilteredLatestRates("EUR", "CZK,USD");

        assertNotNull(result);
        assertEquals(2, result.size(), "List by měl obsahovat pouze 2 platné a nenulové měny");

        List<String> resultCurrencies = result.stream()
                .map(ExchangeRate::getCurrency)
                .toList();

        assertTrue(resultCurrencies.contains("CZK"));
        assertTrue(resultCurrencies.contains("USD"));

        assertFalse(resultCurrencies.contains("XYZ"), "Neplatná měna musí být odstraněna");
        assertFalse(resultCurrencies.contains("GBP"), "Měna s null hodnotou musí být odstraněna");
        assertFalse(resultCurrencies.contains("RT"), "Krátký nebo neplatný řetězec musí být zahozen");

        ExchangeRate czkRate = result.stream().filter(r -> r.getCurrency().equals("CZK")).findFirst().get();
        assertEquals(25.0, czkRate.getRate());
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException() {
        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(null);

        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> {
            currencyService.getFilteredLatestRates("EUR", "");
        });

        assertTrue(exception.getMessage().contains("aktuální kurzy z burzy"));
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException_WhenSuccessIsFalse() {
        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(false);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> {
            currencyService.getFilteredLatestRates("EUR", "");
        });
    }

    @Test
    void testGetFilteredLatestRates_ThrowsException_WhenQuotesAreNull() {
        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(null);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> {
            currencyService.getFilteredLatestRates("EUR", "");
        });
    }
}