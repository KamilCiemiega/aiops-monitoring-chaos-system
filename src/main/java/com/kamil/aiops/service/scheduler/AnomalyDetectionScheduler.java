package com.kamil.aiops.service.scheduler;

import com.kamil.aiops.service.MetricsService;
import com.kamil.aiops.service.SelfHealingService;
import com.kamil.aiops.service.analysis.DataDriftDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionScheduler {

    private final MetricsService metricsService;
    private final DataDriftDetectionService dataDriftDetectionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SelfHealingService selfHealingService;

    @Value("${aiops.target.hostname:unknown-host}")
    private String targetHostname;

    @Scheduled(fixedRate = 15000)
    public void runAnomalyDetection() {
        log.debug("Starting scheduled anomaly detection cycle for host: {}", targetHostname);

        List<Double> recentRamUsage = metricsService.getRecentRamUsage(targetHostname);

        if (recentRamUsage.isEmpty()) {
            log.warn("No telemetry data found for host: {}. Skipping analysis.", targetHostname);
            return;
        }

        double actualCurrentRam = recentRamUsage.get(recentRamUsage.size() - 1);
        Double predictedRam = metricsService.getLatestPrediction(targetHostname, "ram_usage_prediction");

        if (predictedRam == null) {
            predictedRam = actualCurrentRam;
        }

        double mean = recentRamUsage.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = recentRamUsage.stream().mapToDouble(d -> Math.pow(d - mean, 2)).sum() / recentRamUsage.size();
        double stdDev = Math.sqrt(variance);

        double threshold = 3.0 * stdDev;
        double currentDeviation = Math.abs(actualCurrentRam - predictedRam);

        log.info("📊 [AIOps Engine] Actual: {}, Predicted: {}, Current Deviation: {}, Allowed Threshold (3σ): {}",
                actualCurrentRam, predictedRam, currentDeviation, threshold);

        boolean isLeaking = dataDriftDetectionService.detectMemoryLeak(recentRamUsage);
        boolean isAnomalousJump = currentDeviation > threshold && threshold > 0.1;

        if (isLeaking || isAnomalousJump) {
            log.warn("🚨 CRITICAL ALERT: Dynamic threshold breached! Reason: {}",
                    isAnomalousJump ? "Prediction mismatch" : "Data drift detected");

            String alertMessage = isAnomalousJump ? "Unexpected RAM spike deviating from Holt-Winters forecast!" : "Memory leak confirmed!";
            String jsonAlert = String.format(
                    "{\"host\": \"%s\", \"status\": \"CRITICAL\", \"message\": \"%s\"}",
                    targetHostname, alertMessage
            );
            messagingTemplate.convertAndSend("/topic/alerts", jsonAlert);
            log.info("📢 Alert pushed via WebSocket channel /topic/alerts");

            selfHealingService.executeMitigation(alertMessage, targetHostname);

        } else {
            log.debug("Infrastructure health is nominal for host: {}", targetHostname);
        }
    }
}