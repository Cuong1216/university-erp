package com.wiz.universityerpapi.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryForecastResponseDTO {
    private String modelUsed;
    private List<HistoricalPointDTO> historicalActuals;
    private List<ForecastPointDTO> forecastPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalPointDTO {
        private String ds; // YYYY-MM-DD
        private Integer month;
        private Integer year;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPointDTO {
        private String ds; // YYYY-MM-DD
        private BigDecimal yhat;
        private BigDecimal yhatLower;
        private BigDecimal yhatUpper;
    }
}
