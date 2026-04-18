package cz.tul.stin.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtremesResult {
    private String strongestCurrency;
    private double strongestValue;
    private String weakestCurrency;
    private double weakestValue;
}