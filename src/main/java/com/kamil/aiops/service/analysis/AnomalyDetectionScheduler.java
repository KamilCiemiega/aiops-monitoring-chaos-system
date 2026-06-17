package com.kamil.aiops.service.analysis;

import com.kamil.aiops.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionScheduler {

    private final MetricsService metricsService;
    private final DataDriftDetectionService dataDriftDetectionService;

    // For now, we target a specific host.
    // In the future, this could be a dynamic list fetched from an auto-discovery registry.
    @Value("${aiops.target.hostname:unknown-host}")
    private String targetHostname;

    /**
     * Scheduled task that runs every 15 seconds to analyze telemetry data.
     * It extracts recent RAM usage and runs the Data Drift Detection algorithm.
     */
    @Scheduled(fixedRate = 15000)
    public void runAnomalyDetection() {
        log.debug("Starting scheduled anomaly detection cycle for host: {}", targetHostname);

        // 1. Fetch the recent RAM usage data points from InfluxDB
        List<Double> recentRamUsage = metricsService.getRecentRamUsage(targetHostname);

        // 2. Check if we have gathered any telemetry data
        if (recentRamUsage.isEmpty()) {
            log.warn("No telemetry data found for host: {}. Skipping analysis.", targetHostname);
            return;
        }

        // 3. Run the linear regression analysis algorithm
        boolean isLeaking = dataDriftDetectionService.detectMemoryLeak(recentRamUsage);

        // 4. Handle the diagnostic result
        if (isLeaking) {
            log.warn("CRITICAL ALERT: Memory leak confirmed on host {}!", targetHostname);
            // TODO: In Step 4, we will push this alert to the React dashboard via WebSockets
        } else {
            log.debug("Infrastructure health is nominal for host: {}", targetHostname);
        }
    }
}