package cz.tul.stin.backend.client;

import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RealExchangeRateClient implements ExchangeRateClient {

    private final RestTemplate restTemplate;

    @Value("${api.exchangerate.key}")
    private String apiKey;

    @Value("${api.exchangerate.base-url}")
    private String baseUrl;

    @Override
    public LiveRatesResponse getLatestRates(String base, String symbols) {
        log.info("Fetching latest rates from real API. Base: {}, Currencies: {}", base, symbols);

        String url = UriComponentsBuilder.fromUriString(baseUrl + "/live")
                .queryParam("access_key", apiKey)
                .queryParam("source", base)
                .queryParam("currencies", symbols)
                .toUriString();

        log.info("Generated URL for API: {}", url);
        return restTemplate.getForObject(url, LiveRatesResponse.class);
    }

    @Override
    public TimeframeResponse getHistoricalRates(String startDate, String endDate, String base, String symbols) {
        log.info("Fetching historical data from real API. Period: {} to {}", startDate, endDate);

        String url = UriComponentsBuilder.fromUriString(baseUrl + "/timeframe")
                .queryParam("access_key", apiKey)
                .queryParam("start_date", startDate)
                .queryParam("end_date", endDate)
                .queryParam("source", base)
                .queryParam("currencies", symbols)
                .toUriString();

        log.info("Generated URL for API: {}", url);
        return restTemplate.getForObject(url, TimeframeResponse.class);
    }
}