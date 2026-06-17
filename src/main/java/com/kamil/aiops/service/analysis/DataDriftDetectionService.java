package com.kamil.aiops.service.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DataDriftDetectionService {

    /**
     * Threshold for memory leak detection.
     * If the slope (trend) is consistently above this value, it indicates a leak.
     */
    private static final double LEAK_THRESHOLD = 0.05;

    /**
     * Analyzes a list of RAM usage data points to detect data drift (Memory Leak).
     * Uses Simple Linear Regression to calculate the trend slope.
     * * @param ramUsageHistory A list of recent RAM usage percentages.
     * @return true if a memory leak is detected, false otherwise.
     */
    public boolean detectMemoryLeak(List<Double> ramUsageHistory) {
        if (ramUsageHistory == null || ramUsageHistory.size() < 2) {
            log.debug("Not enough data points to calculate drift.");
            return false;
        }

        double slope = calculateLinearRegressionSlope(ramUsageHistory);
        log.info("Calculated RAM drift slope: {}", slope);

        if (slope > LEAK_THRESHOLD) {
            log.warn("🚨 MEMORY LEAK DETECTED! The RAM usage trend is growing unnaturally.");
            return true;
        }

        return false;
    }


    private double calculateLinearRegressionSlope(List<Double> yValues) {
        int n = yValues.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {

            double x = i;
            double y = yValues.get(i);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = (n * sumX2) - (sumX * sumX);

        if (denominator == 0) {
            return 0.0;
        }

        return numerator / denominator;
    }
}