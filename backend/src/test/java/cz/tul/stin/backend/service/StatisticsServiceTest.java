package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;
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
class StatisticsServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void testFindExtremes_Success() {
        Map<String, Double> mockQuotes = new HashMap<>();
        mockQuotes.put("EURCZK", 25.0);
        mockQuotes.put("EURUSD", 1.1);
        mockQuotes.put("EURGBP", 0.85);
        mockQuotes.put("EURXYZ", 390.0);
        mockQuotes.put("EURAUD", null);
        mockQuotes.put("EURPLN", 5.0);
        mockQuotes.put("SHORT", 10.0);
        mockQuotes.put(null, 50.0);

        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(mockQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK,USD,GBP,XYZ"))
                .thenReturn(mockResponse);

        ExtremesResult result = statisticsService.findExtremes("EUR", "CZK,USD,GBP,XYZ");

        assertNotNull(result);
        assertEquals("CZK", result.getStrongestCurrency());
        assertEquals(25.0, result.getStrongestValue());
        assertEquals("GBP", result.getWeakestCurrency());
        assertEquals(0.85, result.getWeakestValue());
    }

    @Test
    void testFindExtremes_Success_WithNullSymbols() {
        Map<String, Double> mockQuotes = new HashMap<>();
        mockQuotes.put("EURCZK", 25.0);
        mockQuotes.put("EURUSD", 1.1);

        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(mockQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", null))
                .thenReturn(mockResponse);

        ExtremesResult result = statisticsService.findExtremes("EUR", null);

        assertNotNull(result);
        assertEquals("CZK", result.getStrongestCurrency());
        assertEquals("USD", result.getWeakestCurrency());
    }

    @Test
    void testCalculateAverages_Success_IgnoresEmptyData() {
        Map<String, Double> day1 = new HashMap<>();
        day1.put("EURCZK", 24.0);
        day1.put("EURUSD", 1.0);
        day1.put("EURAUD", null);
        day1.put("EURXYZ", 999.0);
        day1.put("EURPLN", 5.0);

        Map<String, Double> day2 = new HashMap<>();

        Map<String, Double> day3 = new HashMap<>();
        day3.put("EURCZK", 25.0);
        day3.put("EURUSD", 1.2);

        Map<String, Map<String, Double>> quotes = new HashMap<>();
        quotes.put("2025-01-01", day1);
        quotes.put("2025-01-02", day2);
        quotes.put("2025-01-03", day3);
        quotes.put("2025-01-04", null);

        TimeframeResponse mockResponse = new TimeframeResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(quotes);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK,USD"))
                .thenReturn(mockResponse);

        Map<String, Double> averages = statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK,USD");

        assertNotNull(averages);
        assertEquals(24.5, averages.get("CZK"));
        assertEquals(1.1, averages.get("USD"));
        assertFalse(averages.containsKey("PLN"));
    }

    @Test
    void testCalculateAverages_Success_WithEmptySymbols() {
        Map<String, Double> day1 = new HashMap<>();
        day1.put("EURCZK", 24.0);
        day1.put("EURXYZ", 999.0);
        day1.put("EURAUD", null);
        day1.put("BAD", 10.0);

        Map<String, Map<String, Double>> quotes = new HashMap<>();
        quotes.put("2025-01-01", day1);

        TimeframeResponse mockResponse = new TimeframeResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(quotes);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-01", "EUR", ""))
                .thenReturn(mockResponse);

        Map<String, Double> averages = statisticsService.calculateAverages("2025-01-01", "2025-01-01", "EUR", "");

        assertNotNull(averages);
        assertEquals(24.0, averages.get("CZK"));
        assertFalse(averages.containsKey("XYZ"));
        assertFalse(averages.containsKey("AUD"));
    }

    @Test
    void testFindExtremes_ThrowsException() {
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK"))
                .thenReturn(null);

        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void testCalculateAverages_ThrowsException() {
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK"))
                .thenReturn(null);

        assertThrows(ExternalApiException.class, () -> statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"));
    }

    @Test
    void testFindExtremes_ThrowsException_WhenSuccessIsFalse() {
        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void testCalculateAverages_ThrowsException_WhenSuccessIsFalse() {
        TimeframeResponse mockResponse = new TimeframeResponse();
        mockResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"));
    }

    @Test
    void testFindExtremes_ThrowsException_WhenQuotesAreNull() {
        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(null);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void testCalculateAverages_ThrowsException_WhenQuotesAreNull() {
        TimeframeResponse mockResponse = new TimeframeResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(null);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"));
    }
}