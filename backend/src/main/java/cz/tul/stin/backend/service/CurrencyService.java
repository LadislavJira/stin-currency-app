package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final ExchangeRateClient exchangeRateClient;

    public Map<String, Double> getFilteredLatestRates(String baseCurrency) {
        LatestRatesResponse response = exchangeRateClient.getLatestRates(baseCurrency, "");

        if (response == null || !response.isSuccess() || response.getRates() == null) {
            throw new RuntimeException("Nepodařilo se získat data z API");
        }

        Map<String, Double> filteredRates = new HashMap<>();

        for (Map.Entry<String, Double> entry : response.getRates().entrySet()) {
            if (CurrencySymbol.isValid(entry.getKey())) {
                filteredRates.put(entry.getKey(), entry.getValue());
            }
        }

        return filteredRates;
    }
}
