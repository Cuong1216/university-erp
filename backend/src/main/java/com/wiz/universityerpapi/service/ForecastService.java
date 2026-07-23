package com.wiz.universityerpapi.service;

import com.wiz.universityerpapi.dto.analytics.SalaryForecastResponseDTO;
import com.wiz.universityerpapi.dto.analytics.SalaryForecastResponseDTO.ForecastPointDTO;
import com.wiz.universityerpapi.dto.analytics.SalaryForecastResponseDTO.HistoricalPointDTO;
import com.wiz.universityerpapi.repository.BangLuongThangRepository;
import com.wiz.universityerpapi.repository.projection.MonthlySalaryTrendView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final BangLuongThangRepository bangLuongThangRepository;

    @Value("${ai.forecaster.url:http://localhost:8001/forecast}")
    private String forecasterUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional(readOnly = true)
    @Cacheable(value = "salary_forecast", key = "'trend_forecast'")
    public SalaryForecastResponseDTO getSalaryForecast(int periods) {
        log.info("Tính toán và dự báo quỹ lương cho {} kỳ tới qua AI microservice...", periods);

        // 1. Lấy dữ liệu lịch sử từ DB (tối đa 24 tháng gần nhất)
        List<MonthlySalaryTrendView> rawTrends = bangLuongThangRepository.findSalaryTrends(PageRequest.of(0, 24));
        if (rawTrends.isEmpty()) {
            return new SalaryForecastResponseDTO("No Data", Collections.emptyList(), Collections.emptyList());
        }

        // Sắp xếp tăng dần theo thời gian (cũ -> mới)
        List<MonthlySalaryTrendView> sortedTrends = rawTrends.stream()
                .sorted(Comparator.comparing(MonthlySalaryTrendView::getNam)
                        .thenComparing(MonthlySalaryTrendView::getThang))
                .collect(Collectors.toList());

        List<HistoricalPointDTO> historicalList = new ArrayList<>();
        List<Map<String, Object>> requestHistoryPayload = new ArrayList<>();

        for (MonthlySalaryTrendView v : sortedTrends) {
            String ds = String.format("%04d-%02d-01", v.getNam(), v.getThang());
            BigDecimal amount = v.getTongTienLuong() != null ? v.getTongTienLuong() : BigDecimal.ZERO;

            historicalList.add(new HistoricalPointDTO(ds, v.getThang(), v.getNam(), amount));

            Map<String, Object> point = new HashMap<>();
            point.put("ds", ds);
            point.put("y", amount.doubleValue());
            requestHistoryPayload.add(point);
        }

        // 2. Chuẩn bị payload gửi sang Python FastAPI Forecaster Service
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("history", requestHistoryPayload);
        reqBody.put("periods", periods > 0 ? periods : 6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(forecasterUrl, entity, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> body = resp.getBody();
                String modelUsed = (String) body.getOrDefault("model_used", "Time Series Hybrid Engine");
                List<Map<String, Object>> forecastPointsRaw = (List<Map<String, Object>>) body.get("forecast");

                List<ForecastPointDTO> forecastList = new ArrayList<>();
                if (forecastPointsRaw != null) {
                    for (Map<String, Object> fp : forecastPointsRaw) {
                        String ds = (String) fp.get("ds");
                        double yhat = ((Number) fp.get("yhat")).doubleValue();
                        double yhatLower = ((Number) fp.get("yhat_lower")).doubleValue();
                        double yhatUpper = ((Number) fp.get("yhat_upper")).doubleValue();
                        forecastList.add(new ForecastPointDTO(ds, BigDecimal.valueOf(yhat), BigDecimal.valueOf(yhatLower), BigDecimal.valueOf(yhatUpper)));
                    }
                }

                return new SalaryForecastResponseDTO(modelUsed, historicalList, forecastList);
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối Python Forecaster AI Service tại {}: {}", forecasterUrl, e.getMessage());
        }

        // Fallback tự động: nếu Python microservice chưa lên hoặc lỗi kết nối, tính toán dự báo xu hướng đơn giản ngay tại Java
        return fallbackLocalForecast(historicalList, periods > 0 ? periods : 6);
    }

    @CacheEvict(value = "salary_forecast", allEntries = true)
    public void evictForecastCache() {
        log.info("Xóa cache salary_forecast sau khi có thay đổi bảng lương.");
    }

    private SalaryForecastResponseDTO fallbackLocalForecast(List<HistoricalPointDTO> historicalList, int periods) {
        log.info("Thực hiện fallback dự báo xu hướng tuyến tính tại Java Engine...");
        int n = historicalList.size();
        if (n == 0) return new SalaryForecastResponseDTO("Java Fallback Engine (No Data)", historicalList, Collections.emptyList());

        double avg = historicalList.stream()
                .map(HistoricalPointDTO::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(100000000.0);

        // Xu hướng tăng trưởng trung bình mỗi tháng 2%
        List<ForecastPointDTO> forecastList = new ArrayList<>();
        HistoricalPointDTO lastPoint = historicalList.get(n - 1);
        int lastMonth = lastPoint.getMonth();
        int lastYear = lastPoint.getYear();

        for (int i = 1; i <= periods; i++) {
            int nextMonth = lastMonth + i;
            int nextYear = lastYear;
            while (nextMonth > 12) {
                nextMonth -= 12;
                nextYear++;
            }
            String ds = String.format("%04d-%02d-01", nextYear, nextMonth);
            
            double seasonalMult = (nextMonth == 9 || nextMonth == 10) ? 1.10 : (nextMonth == 2 || nextMonth == 3 ? 1.05 : 1.0);
            double val = avg * (1 + 0.015 * i) * seasonalMult;
            double lower = val * 0.92;
            double upper = val * 1.08;

            forecastList.add(new ForecastPointDTO(
                    ds,
                    BigDecimal.valueOf(Math.round(val)),
                    BigDecimal.valueOf(Math.round(lower)),
                    BigDecimal.valueOf(Math.round(upper))
            ));
        }

        return new SalaryForecastResponseDTO("Java Hybrid Linear-Seasonal Fallback Engine", historicalList, forecastList);
    }
}
