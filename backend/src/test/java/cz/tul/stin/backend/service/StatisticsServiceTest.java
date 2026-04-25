package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.HistoryResponse;
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
    void validateCurrencies_ThrowsException_WhenSymbolsAreNullOrEmpty() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getExtremes("EUR", null));
        assertEquals("error.currency.empty", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getExtremes("EUR", "   "));
        assertEquals("error.currency.empty", ex2.getMessage());
    }

    @Test
    void validateCurrencies_ThrowsException_WhenBaseOrSymbolIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getExtremes("XYZ", "CZK"));

        assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getExtremes("EUR", "CZK,BLA"));
    }

    @Test
    void validateDates_ThrowsException_ForInvalidDates() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getHistory("EUR", "CZK", "2026-05-01", "2026-04-01"));
        assertEquals("error.date.invalid", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getHistory("EUR", "CZK", "2026/04/01", "2026-04-24"));
        assertEquals("error.date.format", ex2.getMessage());

        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> statisticsService.getHistory("EUR", "CZK", "2020-01-01", "2021-01-05"));
        assertEquals("error.date.tooLong", ex3.getMessage());
    }

    @Test
    void getExtremes_ThrowsException_WhenApiFails() {
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(null);
        assertThrows(ExternalApiException.class, () -> statisticsService.getExtremes("EUR", "CZK"));

        LiveRatesResponse failResponse = new LiveRatesResponse();
        failResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(failResponse);
        assertThrows(ExternalApiException.class, () -> statisticsService.getExtremes("EUR", "CZK"));

        LiveRatesResponse noQuotesResponse = new LiveRatesResponse();
        noQuotesResponse.setSuccess(true);
        noQuotesResponse.setQuotes(null);
        Mockito.when(exchangeRateClient.getLatestRates(anyString(), anyString())).thenReturn(noQuotesResponse);
        assertThrows(ExternalApiException.class, () -> statisticsService.getExtremes("EUR", "CZK"));
    }

    @Test
    void getHistory_ThrowsException_WhenApiFails() {
        Mockito.when(exchangeRateClient.getHistoricalRates(anyString(), anyString(), anyString(), anyString())).thenReturn(null);
        assertThrows(ExternalApiException.class, () -> statisticsService.getHistory("EUR", "CZK", "2026-04-01", "2026-04-02"));
    }

    @Test
    void getExtremes_Success_CoversAllMappingBranches() {
        Map<String, Double> quotes = new LinkedHashMap<>();
        quotes.put(null, 1.0);
        quotes.put("SHORT", 2.0);
        quotes.put("EURCZK", null);
        quotes.put("EURXYZ", 3.0);
        quotes.put("EURGBP", 4.0);
        quotes.put("EURCZK", 25.0);
        quotes.put("EURUSD", 1.0);
        quotes.put("EURPLN", 5.0);

        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        mockResponse.setQuotes(quotes);

        Mockito.when(exchangeRateClient.getLatestRates("EUR", "CZK,USD,PLN"))
                .thenReturn(mockResponse);

        ExtremesResult result = statisticsService.getExtremes("EUR", "CZK,USD,PLN");

        assertNotNull(result);
        assertEquals("CZK", result.getStrongestCurrency(), "Nejvyšší kurz má být CZK (25.0)");
        assertEquals(25.0, result.getStrongestValue());
        assertEquals("USD", result.getWeakestCurrency(), "Nejnižší kurz má být USD (1.0)");
        assertEquals(1.0, result.getWeakestValue());
    }

    @Test
    void getHistory_Success_CoversAveragesAndChartFilters() {

        Map<String, Double> dayBefore = new HashMap<>();
        dayBefore.put("EURCZK", 99.0);

        Map<String, Double> dayNullValues = new HashMap<>();
        dayNullValues.put("EURCZK", null);
        dayNullValues.put("SHORT", 5.0);

        Map<String, Double> dayValid1 = new HashMap<>();
        dayValid1.put("EURCZK", 25.111);
        dayValid1.put("EURUSD", 1.0);

        Map<String, Double> dayValid2 = new HashMap<>();
        dayValid2.put("EURCZK", 25.222);
        Map<String, Double> dayAfter = new HashMap<>();
        dayAfter.put("EURCZK", 99.0);

        Map<String, Map<String, Double>> timeframeQuotes = new HashMap<>();
        timeframeQuotes.put("2026-03-31", dayBefore);
        timeframeQuotes.put("2026-04-01", null);
        timeframeQuotes.put("2026-04-02", dayNullValues);
        timeframeQuotes.put("2026-04-03", dayValid1);
        timeframeQuotes.put("2026-04-04", dayValid2);
        timeframeQuotes.put("2026-04-05", dayAfter);

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(timeframeQuotes);

        Mockito.when(exchangeRateClient.getHistoricalRates(eq("2026-04-01"), eq("2026-04-04"), eq("EUR"), eq("CZK,USD")))
                .thenReturn(timeframeResponse);


        HistoryResponse result = statisticsService.getHistory("EUR", "CZK,USD", "2026-04-01", "2026-04-04");

        Map<String, Map<String, Double>> timeseries = result.getTimeseries();
        assertFalse(timeseries.containsKey("2026-03-31"), "Datum před rozsahem nesmí být v datech.");
        assertFalse(timeseries.containsKey("2026-04-01"), "Datum s null daty nesmí projít.");
        assertFalse(timeseries.containsKey("2026-04-05"), "Datum po rozsahu nesmí být v datech.");
        assertTrue(timeseries.containsKey("2026-04-03"));
        assertEquals(25.111, timeseries.get("2026-04-03").get("CZK"));
        assertEquals(1.0, timeseries.get("2026-04-03").get("USD"));

        Map<String, Double> averages = result.getAverages();
       assertEquals(25.167, averages.get("CZK"), "Průměr CZK musí být správně zaokrouhlen na 3 desetinná místa");
        assertEquals(1.0, averages.get("USD"));
    }
   
    @Test
    void getHistory_ThrowsException_WhenApiFails_Branches() {
        TimeframeResponse failResponse = new TimeframeResponse();
        failResponse.setSuccess(false);
        Mockito.when(exchangeRateClient.getHistoricalRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(failResponse);
        assertThrows(ExternalApiException.class, () -> statisticsService.getHistory("EUR", "CZK", "2026-04-01", "2026-04-02"));

        TimeframeResponse noQuotesResponse = new TimeframeResponse();
        noQuotesResponse.setSuccess(true);
        noQuotesResponse.setQuotes(null);
        Mockito.when(exchangeRateClient.getHistoricalRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(noQuotesResponse);
        assertThrows(ExternalApiException.class, () -> statisticsService.getHistory("EUR", "CZK", "2026-04-01", "2026-04-02"));
    }

    @Test
    void getHistory_Success_CoversBuildTimeseriesBranches() {
        Map<String, Double> dailyQuotes = new HashMap<>();
        dailyQuotes.put(null, 1.0);
        dailyQuotes.put("SHORT", 2.0);
        dailyQuotes.put("EURGBP", 3.0);
        dailyQuotes.put("EURCZK", 25.0);

        Map<String, Map<String, Double>> timeframeQuotes = new HashMap<>();
        timeframeQuotes.put("2026-04-01", dailyQuotes);

        TimeframeResponse timeframeResponse = new TimeframeResponse();
        timeframeResponse.setSuccess(true);
        timeframeResponse.setQuotes(timeframeQuotes);

        Mockito.when(exchangeRateClient.getHistoricalRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(timeframeResponse);

        HistoryResponse result = statisticsService.getHistory("EUR", "CZK", "2026-04-01", "2026-04-02");

        assertTrue(result.getTimeseries().get("2026-04-01").containsKey("CZK"));
        assertFalse(result.getTimeseries().get("2026-04-01").containsKey("GBP"), "GBP nebylo vyžádáno, nesmí tam být");
    }

    @Test
    void parseSymbols_ReturnsEmptyList_ForNullOrEmpty() throws Exception {
        java.lang.reflect.Method method = StatisticsService.class.getDeclaredMethod("parseSymbols", String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> resultNull = (java.util.List<String>) method.invoke(statisticsService, (String) null);
        assertTrue(resultNull.isEmpty(), "Mělo by vrátit prázdný list, pokud je vstup null");

        @SuppressWarnings("unchecked")
        java.util.List<String> resultEmpty = (java.util.List<String>) method.invoke(statisticsService, "");
        assertTrue(resultEmpty.isEmpty(), "Mělo by vrátit prázdný list, pokud je vstup prázdný");
    }
}