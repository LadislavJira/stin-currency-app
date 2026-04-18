package cz.tul.stin.backend.model;

import lombok.Data;

@Data
public class ExchangeRate {
    private String currency;
    private Double rate;
    private String date;
}