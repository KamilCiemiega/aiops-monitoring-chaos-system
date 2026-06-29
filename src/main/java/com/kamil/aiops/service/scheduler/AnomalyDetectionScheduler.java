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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        boolean isRamCritical = false;

        if (!recentRamUsage.isEmpty()) {
            double actualCurrentRam = recentRamUsage.getLast();
            Double predictedRam = metricsService.getLatestPrediction(targetHostname, "ram_usage_prediction");

            if (predictedRam == null) {
                predictedRam = actualCurrentRam;
            }

            double meanRam = recentRamUsage.stream().mapToDouble(d -> d).average().orElse(0.0);
            double varianceRam = recentRamUsage.stream().mapToDouble(d -> Math.pow(d - meanRam, 2)).sum() / recentRamUsage.size();
            double stdDevRam = Math.sqrt(varianceRam);
            double thresholdRam = 3.0 * stdDevRam;
            double currentDeviationRam = Math.abs(actualCurrentRam - predictedRam);

            log.info("📊 [RAM Engine] Actual: {}, Predicted: {}, Deviation: {}, Threshold (3σ): {}",
                    actualCurrentRam, predictedRam, currentDeviationRam, thresholdRam);

            boolean isLeaking = dataDriftDetectionService.detectMemoryLeak(recentRamUsage);
            boolean isRamJump = currentDeviationRam > thresholdRam && thresholdRam > 0.01;

            if (isLeaking || isRamJump) {
                isRamCritical = true;
                String alertMessage = isRamJump ? "Unexpected RAM spike deviating from Holt-Winters forecast!" : "Memory leak confirmed!";
                triggerAlert(alertMessage);
            }
        }

        List<Double> recentCpuUsage = metricsService.getRecentCpuUsage(targetHostname);

        if (!recentCpuUsage.isEmpty() && !isRamCritical) {
            double actualCurrentCpu = recentCpuUsage.get(recentCpuUsage.size() - 1);
            Double predictedCpu = metricsService.getLatestPrediction(targetHostname, "cpu_usage_prediction");

            if (predictedCpu != null) {
                double meanCpu = recentCpuUsage.stream().mapToDouble(d -> d).average().orElse(0.0);
                double varianceCpu = recentCpuUsage.stream().mapToDouble(d -> Math.pow(d - meanCpu, 2)).sum() / recentCpuUsage.size();
                double stdDevCpu = Math.sqrt(varianceCpu);

                double thresholdCpu = 3.0 * stdDevCpu;
                double currentDeviationCpu = Math.abs(actualCurrentCpu - predictedCpu);

                log.info("📊 [CPU Engine] Actual: {}, Predicted: {}, Deviation: {}, Threshold (3σ): {}",
                        actualCurrentCpu, predictedCpu, currentDeviationCpu, thresholdCpu);

                if (currentDeviationCpu > thresholdCpu && actualCurrentCpu > 70.0) {
                    triggerAlert("CPU spike confirmed!");
                }
            }
        }
    }

    private void triggerAlert(String alertMessage) {
        log.warn("🚨 CRITICAL ALERT: Dynamic threshold breached! Reason: {}", alertMessage);

        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String jsonAlert = String.format(
                "{\"host\": \"%s\", \"status\": \"CRITICAL\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                targetHostname, alertMessage, currentTimestamp
        );

        messagingTemplate.convertAndSend("/topic/alerts", jsonAlert);
        log.info("📢 Alert pushed via WebSocket channel /topic/alerts");

        selfHealingService.executeMitigation(alertMessage, targetHostname);
    }
}