package cz.tul.stin.backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class HistoryResponse {
    private Map<String, Double> averages;
    private Map<String, Map<String, Double>> timeseries;
}