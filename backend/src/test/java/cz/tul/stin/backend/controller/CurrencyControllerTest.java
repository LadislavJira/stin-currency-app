package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.HistoryResponse;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    @WithMockUser
    void testGetAvailableSymbols_ReturnsListOfCurrencies() throws Exception {
        List<String> mockSymbols = Arrays.asList("EUR", "CZK", "USD");
        Mockito.when(currencyService.getAvailableSymbols()).thenReturn(mockSymbols);

        mockMvc.perform(get("/api/currencies/symbols")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1]").value("CZK"));
    }

    @Test
    @WithMockUser
    void testGetExtremes_WithValidParams_ReturnsExtremesResult() throws Exception {
        ExtremesResult mockResponse = new ExtremesResult("CZK", 25.0, "USD", 1.1);

        Mockito.when(statisticsService.getExtremes(eq("EUR"), eq("CZK,USD")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/extremes")
                        .param("base", "EUR")
                        .param("symbols", "CZK,USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strongestCurrency").value("CZK"))
                .andExpect(jsonPath("$.strongestValue").value(25.0))
                .andExpect(jsonPath("$.weakestCurrency").value("USD"))
                .andExpect(jsonPath("$.weakestValue").value(1.1));
    }

    @Test
    @WithMockUser
    void testGetExtremes_UsesDefaultBaseCurrency_WhenBaseIsMissing() throws Exception {
        ExtremesResult mockResponse = new ExtremesResult("CZK", 25.0, "USD", 1.1);

        Mockito.when(statisticsService.getExtremes(eq("EUR"), eq("CZK")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/extremes")
                        .param("symbols", "CZK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(statisticsService).getExtremes("EUR", "CZK");
    }

    @Test
    @WithMockUser
    void testGetExtremes_WithoutRequiredParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/currencies/extremes")
                        .param("base", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetHistory_WithValidParams_ReturnsHistoryResponse() throws Exception {
        Map<String, Double> averages = new HashMap<>();
        averages.put("CZK", 24.5);
        Map<String, Map<String, Double>> timeseries = new HashMap<>();

        HistoryResponse mockResponse = HistoryResponse.builder()
                .averages(averages)
                .timeseries(timeseries)
                .build();

        Mockito.when(statisticsService.getHistory(eq("EUR"), eq("CZK,USD"), eq("2026-04-01"), eq("2026-04-24")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/history")
                        .param("base", "EUR")
                        .param("symbols", "CZK,USD")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-24")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averages.CZK").value(24.5));
    }

    @Test
    @WithMockUser
    void testGetHistory_UsesDefaultBaseCurrency_WhenBaseIsMissing() throws Exception {
        HistoryResponse mockResponse = HistoryResponse.builder().build();

        Mockito.when(statisticsService.getHistory(eq("EUR"), eq("CZK"), eq("2026-04-01"), eq("2026-04-24")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/history")
                        .param("symbols", "CZK")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-24")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(statisticsService).getHistory("EUR", "CZK", "2026-04-01", "2026-04-24");
    }

    @Test
    @WithMockUser
    void testGetHistory_WithoutRequiredParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/currencies/history")
                        .param("base", "EUR"))
                .andExpect(status().isBadRequest());
    }
}
