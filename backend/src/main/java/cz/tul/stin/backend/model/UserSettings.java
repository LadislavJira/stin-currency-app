package cz.tul.stin.backend.model;

import lombok.Data;
import java.util.List;

@Data
public class UserSettings {
    private String baseCurrency;
    private List<String> selectedCurrencies;
}