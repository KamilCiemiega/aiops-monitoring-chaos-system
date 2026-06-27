package com.kamil.aiops.service.scheduler;

import com.kamil.aiops.service.MetricsService;
import com.kamil.aiops.service.analysis.HoltWintersPredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionSchedulerTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private HoltWintersPredictionService holtWintersPredictionService;

    @InjectMocks
    private PredictionScheduler predictionScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(predictionScheduler, "targetHostname", "KCiemiegaTB16");
    }

    @Test
    void shouldGenerateAndSavePredictionsWhenEnoughDataIsAvailable() {
        List<Double> mockCpuHistory = List.of(10.0, 12.0, 11.0, 13.0, 12.0, 14.0, 13.0, 15.0, 14.0, 16.0);
        List<Double> mockPredictions = List.of(17.0, 18.0, 19.0, 20.0, 21.0);

        when(metricsService.getRecentCpuUsage("KCiemiegaTB16")).thenReturn(mockCpuHistory);
        when(holtWintersPredictionService.predict(eq(mockCpuHistory), anyInt(), anyInt())).thenReturn(mockPredictions);

        predictionScheduler.runForecasting();

        verify(metricsService, times(5)).savePredictionMetric(eq("KCiemiegaTB16"), eq("cpu_usage_prediction"), anyDouble(), any(Instant.class));
    }

    @Test
    void shouldSkipForecastingWhenDataIsInsufficient() {
        List<Double> shortCpuHistory = List.of(10.0, 12.0);

        when(metricsService.getRecentCpuUsage("KCiemiegaTB16")).thenReturn(shortCpuHistory);

        predictionScheduler.runForecasting();

        verify(holtWintersPredictionService, never()).predict(anyList(), anyInt(), anyInt());
        verify(metricsService, never()).savePredictionMetric(anyString(), anyString(), anyDouble(), any(Instant.class));
    }

    @Test
    void shouldLogExceptionWhenErrorOccursDuringForecasting() {
        when(metricsService.getRecentCpuUsage("KCiemiegaTB16"))
                .thenThrow(new RuntimeException("Database connection timeout"));

        predictionScheduler.runForecasting();

        verify(holtWintersPredictionService, never()).predict(anyList(), anyInt(), anyInt());
        verify(metricsService, never()).savePredictionMetric(anyString(), anyString(), anyDouble(), any(Instant.class));
    }
}