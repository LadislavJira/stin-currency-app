package cz.tul.stin.backend.model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private ExtremesResult extremes;

    private Map<String, Double> averages;

    private Map<String, Map<String, Double>> timeseries;
}