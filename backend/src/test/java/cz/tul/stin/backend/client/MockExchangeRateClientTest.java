package cz.tul.stin.backend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MockExchangeRateClientTest {

    private MockExchangeRateClient client;

    @BeforeEach
    void setUp() {
        client = new MockExchangeRateClient(new ObjectMapper());
    }

    @Test
    void testGetLatestRates() {
        LatestRatesResponse response = client.getLatestRates("EUR", "CZK");

        assertNotNull(response, "Odpověď nesmí být null");
        assertTrue(response.isSuccess(), "Atribut success musí být true");
        assertEquals("EUR", response.getBase(), "Základní měna by měla být EUR");
        assertNotNull(response.getRates(), "Mapa s kurzy nesmí chybět");
    }

    @Test
    void testGetHistoricalRates() {
        TimeseriesResponse response = client.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK");

        assertNotNull(response, "Odpověď nesmí být null");
        assertTrue(response.isSuccess(), "Atribut success musí být true");
        assertTrue(response.isTimeseries(), "Musí jít o timeseries data");
        assertNotNull(response.getRates(), "Mapa s historickými kurzy nesmí chybět");
    }

    @Test
    void testGetLatestRates_ThrowsException() throws Exception {
        ObjectMapper brokenMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(brokenMapper.readValue(Mockito.any(java.io.InputStream.class), Mockito.eq(LatestRatesResponse.class)))
                .thenThrow(new RuntimeException("Simulovaná chyba"));

        MockExchangeRateClient brokenClient = new MockExchangeRateClient(brokenMapper);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            brokenClient.getLatestRates("EUR", "CZK");
        });
        assertTrue(exception.getMessage().contains("Chyba při načítání"));
    }

    @Test
    void testGetHistoricalRates_ThrowsException() throws Exception {
        ObjectMapper brokenMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(brokenMapper.readValue(Mockito.any(java.io.InputStream.class), Mockito.eq(TimeseriesResponse.class)))
                .thenThrow(new RuntimeException("Simulovaná chyba historie"));

        MockExchangeRateClient brokenClient = new MockExchangeRateClient(brokenMapper);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            brokenClient.getHistoricalRates("2025-01-01", "2025-01-03", "EUR", "CZK");
        });

        assertTrue(exception.getMessage().contains("Chyba při načítání mock dat pro historii"),
                "Zpráva výjimky musí obsahovat text z catch bloku");
    }
}