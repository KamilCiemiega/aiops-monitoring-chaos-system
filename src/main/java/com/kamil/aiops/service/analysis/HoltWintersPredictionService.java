package com.kamil.aiops.service.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HoltWintersPredictionService {

    // Smoothing parameters (can be extracted to application.yaml for easier tuning later)
    private static final double ALPHA = 0.3; // Data smoothing factor
    private static final double BETA = 0.1;  // Trend smoothing factor
    private static final double GAMMA = 0.2; // Seasonal smoothing factor

    /**
     * Generates a forecast using the Additive Holt-Winters method.
     *
     * @param data The historical time-series data (e.g., CPU or RAM usage)
     * @param period The length of the seasonal cycle (e.g., 10 for a 10-second repeating pattern)
     * @param forecastLength The number of future points to predict
     * @return A list of predicted future values
     */
    public List<Double> predict(List<Double> data, int period, int forecastLength) {
        if (data == null || data.size() < 2 * period) {
            log.warn("Not enough data to perform Holt-Winters prediction. Need at least 2 full periods.");
            return new ArrayList<>();
        }

        int n = data.size();
        double[] level = new double[n];
        double[] trend = new double[n];
        double[] season = new double[n + forecastLength];

        // 1. Initialization
        level[0] = data.get(0);
        trend[0] = calculateInitialTrend(data, period);
        for (int i = 0; i < period; i++) {
            season[i] = data.get(i) - level[0];
        }

        // 2. Holt-Winters Additive Algorithm Calculation
        for (int t = 1; t < n; t++) {
            double val = data.get(t);
            double prevLevel = level[t - 1];
            double prevTrend = trend[t - 1];
            double prevSeason = (t >= period) ? season[t - period] : season[t];

            // Update components based on the mathematical equations
            level[t] = ALPHA * (val - prevSeason) + (1 - ALPHA) * (prevLevel + prevTrend);
            trend[t] = BETA * (level[t] - prevLevel) + (1 - BETA) * prevTrend;
            season[t] = GAMMA * (val - level[t]) + (1 - GAMMA) * prevSeason;
        }

        // 3. Forecasting future values
        List<Double> predictions = new ArrayList<>();
        double lastLevel = level[n - 1];
        double lastTrend = trend[n - 1];

        for (int m = 1; m <= forecastLength; m++) {
            int seasonIndex = (n - 1) - period + 1 + ((m - 1) % period);
            double forecast = lastLevel + (m * lastTrend) + season[seasonIndex];

            // Ensure percentages don't go below 0% or above 100%
            forecast = Math.max(0.0, Math.min(100.0, forecast));
            predictions.add(forecast);
        }

        log.debug("Generated {} forecasted points using Holt-Winters algorithm.", forecastLength);
        return predictions;
    }

    /**
     * Helper method to calculate the initial trend estimation.
     */
    private double calculateInitialTrend(List<Double> data, int period) {
        double trend = 0.0;
        for (int i = 0; i < period; i++) {
            trend += (data.get(period + i) - data.get(i)) / period;
        }
        return trend / period;
    }
}