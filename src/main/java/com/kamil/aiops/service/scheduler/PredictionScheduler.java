package com.kamil.aiops.service.scheduler;

import com.kamil.aiops.service.MetricsService;
import com.kamil.aiops.service.analysis.HoltWintersPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionScheduler {

    private final MetricsService metricsService;
    private final HoltWintersPredictionService holtWintersPredictionService;

    @Value("${aiops.target.hostname:unknown-host}")
    private String targetHostname;


    @Scheduled(fixedRate = 20000)
    public void runForecasting() {
        log.info("🔮 [AIOps Engine] Executing Holt-Winters forecasting for host: {}", targetHostname);

        try {
            List<Double> cpuHistory = metricsService.getRecentCpuUsage(targetHostname);

            int period = 5;
            int forecastLength = 5;

            if (cpuHistory.size() < (2 * period)) {
                log.warn("⚠️ Not enough data to perform Holt-Winters prediction. Need at least {} points, got: {}. Skipping.",
                        (2 * period), cpuHistory.size());
                return;
            }

            List<Double> predictions = holtWintersPredictionService.predict(cpuHistory, period, forecastLength);

            log.info("✅ Successfully generated {} forecasted points.", predictions.size());

            Instant now = Instant.now();
            for (int i = 0; i < predictions.size(); i++) {
                Instant futureTime = now.plusSeconds((i + 1) * 5L);
                metricsService.savePredictionMetric(targetHostname, "cpu_usage_prediction", predictions.get(i), futureTime);
            }

        } catch (Exception e) {
            log.error("❌ Error during Holt-Winters forecasting execution: {}", e.getMessage(), e);
        }
    }
}