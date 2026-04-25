package cz.tul.stin.backend.client;

import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealExchangeRateClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RealExchangeRateClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(client, "baseUrl", "https://api.test.com");
    }

    @Test
    void getLatestRates_ShouldConstructCorrectUrl_AndReturnResponse() {
        String base = "EUR";
        String symbols = "CZK,USD";
        String expectedUrl = "https://api.test.com/live?access_key=test-api-key&source=EUR&currencies=CZK,USD";

        LiveRatesResponse mockResponse = new LiveRatesResponse();
        mockResponse.setSuccess(true);
        when(restTemplate.getForObject(eq(expectedUrl), eq(LiveRatesResponse.class)))
                .thenReturn(mockResponse);

        LiveRatesResponse actualResponse = client.getLatestRates(base, symbols);

        assertNotNull(actualResponse, "Odpověď by neměla být null");
        assertEquals(true, actualResponse.isSuccess(), "Odpověď by měla mít success=true");

        verify(restTemplate).getForObject(eq(expectedUrl), eq(LiveRatesResponse.class));
    }

    @Test
    void getHistoricalRates_ShouldConstructCorrectUrl_AndReturnResponse() {
        String startDate = "2026-04-01";
        String endDate = "2026-04-24";
        String base = "SEK";
        String symbols = "CZK,HUF,PLN";
        String expectedUrl = "https://api.test.com/timeframe?access_key=test-api-key&start_date=2026-04-01&end_date=2026-04-24&source=SEK&currencies=CZK,HUF,PLN";

        TimeframeResponse mockResponse = new TimeframeResponse();
        mockResponse.setSuccess(true);

        when(restTemplate.getForObject(eq(expectedUrl), eq(TimeframeResponse.class)))
                .thenReturn(mockResponse);

        TimeframeResponse actualResponse = client.getHistoricalRates(startDate, endDate, base, symbols);

        assertNotNull(actualResponse, "Odpověď by neměla být null");
        assertEquals(true, actualResponse.isSuccess(), "Odpověď by měla mít success=true");

        verify(restTemplate).getForObject(eq(expectedUrl), eq(TimeframeResponse.class));
    }
}