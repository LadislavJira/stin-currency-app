package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.dto.DashboardResponse;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getDashboardData_ThrowsException_WhenDateFormatIsInvalid() {

        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getDashboardData("EUR", "CZK", "2025/01/01", "2025-01-02")
        );
    }

    @Test
    void getDashboardData_ThrowsException_WhenStartIsAfterEnd() {
        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getDashboardData("EUR", "CZK", "2025-01-05", "2025-01-01")
        );
    }

    @Test
    void getDashboardData_ThrowsException_WhenBaseCurrencyIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getDashboardData("XYZ", "CZK", "2025-01-01", "2025-01-02")
        );
    }

    @Test
    void getDashboardData_ThrowsException_WhenSymbolCurrencyIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                statisticsService.getDashboardData("EUR", "CZK,BLA", "2025-01-01", "2025-01-02")
        );
    }

    @Test
    void findExtremes_ThrowsException_WhenApiReturnsNull() {
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(null);
        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void findExtremes_ThrowsException_WhenApiReturnsSuccessFalse() {
        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(mockResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void findExtremes_ThrowsException_WhenLiveQuotesAreNull() {
        LiveRatesResponse response = new LiveRatesResponse();
        response.setSuccess(true);
        response.setQuotes(null);
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(response);
        assertThrows(ExternalApiException.class, () -> statisticsService.findExtremes("EUR", "CZK"));
    }

    @Test
    void getDashboardData_ThrowsException_WhenTimeframeResponseIsNull() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(liveResponse);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK")).thenReturn(null);

        assertThrows(ExternalApiException.class, () -> statisticsService.getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-02"));
    }

    @Test
    void getDashboardData_ThrowsException_WhenTimeframeResponseIsSuccessFalse() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(liveResponse);

        TimeframeResponse timeResponse = new TimeframeResponse();
        timeResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK")).thenReturn(timeResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-02"));
    }

    @Test
    void getDashboardData_ThrowsException_WhenTimeframeQuotesAreNull() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(liveResponse);

        TimeframeResponse timeResponse = new TimeframeResponse();
        timeResponse.setSuccess(true);
        timeResponse.setQuotes(null);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK")).thenReturn(timeResponse);

        assertThrows(ExternalApiException.class, () -> statisticsService.getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-02"));
    }

    @Test
    void getDashboardData_Success_CoversAllLogicBranchesAndFilters() {

        Map<String, Double> latestQuotes = new HashMap<>();
        latestQuotes.put("EURCZK", 25.0);
        latestQuotes.put("EURUSD", 1.1);
        latestQuotes.put("EURXYZ", 10.0);
        latestQuotes.put("SHORT", 5.0);
        latestQuotes.put("EURAUD", null);
        latestQuotes.put("EURPLN", 4.0);
        latestQuotes.put("EURPLN", 4.0);

        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(latestQuotes);

        Map<String, Double> day1Valid = new HashMap<>();
        day1Valid.put("EURCZK", 24.0);
        day1Valid.put("EURUSD", 1.0);
        day1Valid.put("SHORT", 5.0);

        Map<String, Double> dayOutBefore = new HashMap<>();
        dayOutBefore.put("EURCZK", 99.0);

        Map<String, Map<String, Double>> timeframeQuotes = new HashMap<>();
        timeframeQuotes.put("2024-12-31", dayOutBefore);
        timeframeQuotes.put("2025-01-01", day1Valid);
        timeframeQuotes.put("2025-01-02", null);

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(timeframeQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK,USD,GBP"))
                .thenReturn(liveResponse);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK,USD,GBP"))
                .thenReturn(timeframeResponse);


        DashboardResponse result = statisticsService.getDashboardData("EUR", "CZK,USD,GBP", "2025-01-01", "2025-01-02");

        ExtremesResult extremes = result.getExtremes();
        assertEquals("CZK", extremes.getStrongestCurrency());
        assertEquals("USD", extremes.getWeakestCurrency());
        assertEquals(25.0, extremes.getStrongestValue());
        assertEquals(1.1, extremes.getWeakestValue());

        Map<String, Double> averages = result.getAverages();
        assertEquals(24.0, averages.get("CZK"));
        assertEquals(1.0, averages.get("USD"));
        assertFalse(averages.containsKey("GBP"), "GBP nemělo data, nesmí se dělit nulou!");

        Map<String, Map<String, Double>> timeseries = result.getTimeseries();
        assertEquals(1, timeseries.size(), "Graf smí obsahovat jen jeden platný den ze zadaného rozsahu.");
        assertTrue(timeseries.containsKey("2025-01-01"));
        assertFalse(timeseries.containsKey("2024-12-31"), "Datum před rozsahem se nesmí načíst.");
        assertFalse(timeseries.containsKey("2025-01-02"), "Datum s null hodnotou se nesmí zpracovat.");
        assertEquals(24.0, timeseries.get("2025-01-01").get("CZK"));
    }


    @Test
    void getDashboardData_Success_WithEmptySymbols() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(new HashMap<>());

        Mockito.when(exchangeRateClient.getLatestRates("EUR", ""))
                .thenReturn(liveResponse);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", ""))
                .thenReturn(timeframeResponse);

        DashboardResponse result = statisticsService.getDashboardData("EUR", "", "2025-01-01", "2025-01-02");
        assertNotNull(result);
        assertTrue(result.getAverages().isEmpty());
    }




    @Test
    void getDashboardData_Success_WithNullSymbolsAndNullKeys() {
        Map<String, Double> latestQuotes = new HashMap<>();
        latestQuotes.put(null, 25.0);
        latestQuotes.put("EURCZK", 25.0);
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(latestQuotes);

        Map<String, Map<String, Double>> timeframeQuotes = new HashMap<>();
        Map<String, Double> day1 = new HashMap<>();
        day1.put(null, 25.0);
        day1.put("SHORT", 5.0);
        day1.put(null, 5.0);
        day1.put("EURPLN", 4.0);
        timeframeQuotes.put("2024-12-31", day1);
        timeframeQuotes.put("2025-01-01", day1);
        timeframeQuotes.put("2025-01-02", null);

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(timeframeQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", null)).thenReturn(liveResponse);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-01", "EUR", null)).thenReturn(timeframeResponse);

        DashboardResponse result = statisticsService.getDashboardData("EUR", null, "2025-01-01", "2025-01-01");
        assertNotNull(result);
    }

    @Test
    void findExtremes_CoversFalseBranchOfWeakestVal() {
        Map<String, Double> latestQuotes = new LinkedHashMap<>();
        latestQuotes.put("EURUSD", 1.0);
        latestQuotes.put("EURCZK", 25.0);

        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(latestQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK,USD")).thenReturn(liveResponse);

        ExtremesResult result = statisticsService.findExtremes("EUR", "CZK,USD");
        assertEquals("CZK", result.getStrongestCurrency());
        assertEquals("USD", result.getWeakestCurrency());
    }

    @Test
    void getDashboardData_ThrowsException_WhenHistoricalQuotesAreNull() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());
        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(liveResponse);

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(null);

        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK"))
                .thenReturn(timeframeResponse);

        assertThrows(ExternalApiException.class, () ->
                statisticsService.getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-02")
        );
    }

    @Test
    void getDashboardData_FiltersDateAfterEndDate() {
        LiveRatesResponse liveResponse = new LiveRatesResponse();
        liveResponse.setSuccess(true);
        liveResponse.setQuotes(new HashMap<>());

        Map<String, Map<String, Double>> timeframeQuotes = new HashMap<>();
        timeframeQuotes.put("2025-01-05", new HashMap<>());

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(timeframeQuotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK")).thenReturn(liveResponse);
        Mockito.when(exchangeRateClient.getHistoricalRates("2025-01-01", "2025-01-02", "EUR", "CZK"))
                .thenReturn(timeframeResponse);

        DashboardResponse result = statisticsService.getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-02");
        assertTrue(result.getTimeseries().isEmpty(), "Datum po endDate muselo být vyfiltrováno.");
    }
}