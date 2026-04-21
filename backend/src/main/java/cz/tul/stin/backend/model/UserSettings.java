package cz.tul.stin.backend.model;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class UserSettings {
    private String baseCurrency = "EUR";;
    private List<String> selectedCurrencies = Arrays.asList("CZK", "USD", "GBP");
}