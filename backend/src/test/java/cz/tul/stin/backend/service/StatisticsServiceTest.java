package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;
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
        Map<String, Double> mockRates = new HashMap<>();
        mockRates.put("CZK", 25.0);
        mockRates.put("USD", 1.1);
        mockRates.put("GBP", 0.85);
        mockRates.put("XYZ", 390.0);
        mockRates.put("AUD", null);

        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(mockRates);

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
    void testCalculateAverages_Success_IgnoresEmptyData() {
        Map<String, Double> day1 = new HashMap<>();
        day1.put("CZK", 24.0);
        day1.put("USD", 1.0);
        day1.put("AUD", null);
        day1.put("XYZ", 999.0);

        Map<String, Double> day2 = new HashMap<>();

        Map<String, Double> day3 = new HashMap<>();
        day3.put("CZK", 25.0);
        day3.put("USD", 1.2);

        Map<String, Map<String, Double>> timeseriesRates = new HashMap<>();
        timeseriesRates.put("2025-01-01", day1);
        timeseriesRates.put("2025-01-02", day2);
        timeseriesRates.put("2025-01-03", day3);
        timeseriesRates.put("2025-01-04", null);

        TimeseriesResponse mockResponse = new TimeseriesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(timeseriesRates);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK,USD"))
                .thenReturn(mockResponse);

        Map<String, Double> averages = statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK,USD");

        assertNotNull(averages);
        assertEquals(24.5, averages.get("CZK"));
        assertEquals(1.1, averages.get("USD"));
    }

    @Test
    void testFindExtremes_ThrowsException() {
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK"))
                .thenReturn(null);

        // Ověření, že metoda správně spadne a vyhodí výjimku
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            statisticsService.findExtremes("EUR", "CZK");
        });

        assertTrue(exception.getMessage().contains("Nepodařilo se získat aktuální data"));
    }

    @Test
    void testCalculateAverages_ThrowsException() {
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK"))
                .thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK");
        });

        assertTrue(exception.getMessage().contains("Nepodařilo se získat historická data"));
    }

    @Test
    void testFindExtremes_ThrowsException_WhenSuccessIsFalse() {
        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void testFindExtremes_ThrowsException_WhenRatesAreNull() {
        LatestRatesResponse mockResponse = new LatestRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(null);
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }


    @Test
    void testCalculateAverages_ThrowsException_WhenSuccessIsFalse() {
        TimeseriesResponse mockResponse = new TimeseriesResponse();
        mockResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"));
    }

    @Test
    void testCalculateAverages_ThrowsException_WhenRatesAreNull() {
        TimeseriesResponse mockResponse = new TimeseriesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setRates(null);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK")).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"));
    }
}