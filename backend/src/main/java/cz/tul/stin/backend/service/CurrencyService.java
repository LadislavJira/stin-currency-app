package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final ExchangeRateClient exchangeRateClient;

    public List<ExchangeRate> getFilteredLatestRates(String baseCurrency, String symbols) {

        LiveRatesResponse response = exchangeRateClient.getLatestRates(baseCurrency, symbols);

        if (response == null || !response.isSuccess() || response.getQuotes() == null) {
            throw new ExternalApiException("Nepodařilo se získat aktuální kurzy z burzy.");
        }

        List<ExchangeRate> domainRates = new ArrayList<>();

        for (Map.Entry<String, Double> entry : response.getQuotes().entrySet()) {
            String currencyPair = entry.getKey();
            Double rate = entry.getValue();
            if (currencyPair == null || currencyPair.length() != 6 || rate == null) {
                continue;
            }
            String targetCurrency = currencyPair.substring(3);

            if (CurrencySymbol.isValid(targetCurrency)) {
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setCurrency(targetCurrency);
                exchangeRate.setRate(rate);

                domainRates.add(exchangeRate);
            }
        }

        return domainRates;
    }
}