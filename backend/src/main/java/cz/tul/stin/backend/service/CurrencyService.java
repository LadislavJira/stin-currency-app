package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final ExchangeRateClient exchangeRateClient;

    public List<ExchangeRate> getFilteredLatestRates(String baseCurrency) {
        LatestRatesResponse response = exchangeRateClient.getLatestRates(baseCurrency, "");

        if (response == null || !response.isSuccess() || response.getRates() == null) {
            throw new RuntimeException("Nepodařilo se získat data z API");
        }

        List<ExchangeRate> domainRates = new ArrayList<>();

        for (Map.Entry<String, Double> entry : response.getRates().entrySet()) {
            String currency = entry.getKey();
            Double rate = entry.getValue();

            if (rate != null && CurrencySymbol.isValid(currency)) {
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setCurrency(currency);
                exchangeRate.setRate(rate);

                domainRates.add(exchangeRate);
            }
        }

        return domainRates;
    }
}
