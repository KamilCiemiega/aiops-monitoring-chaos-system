package com.kamil.aiops.service.scheduler;

import com.kamil.aiops.service.MetricsService;
import com.kamil.aiops.service.SelfHealingService;
import com.kamil.aiops.service.analysis.DataDriftDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionSchedulerTest {

    @Mock
    private MetricsService metricsService;
    @Mock
    private DataDriftDetectionService dataDriftDetectionService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private SelfHealingService selfHealingService;

    @InjectMocks
    private AnomalyDetectionScheduler anomalyDetectionScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(anomalyDetectionScheduler, "targetHostname", "KCiemiegaTB16");
    }

    @Test
    void shouldTriggerAlertAndSelfHealingWhenDynamicThresholdIsBreached() {
        List<Double> mockRamData = List.of(50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 200.0);

        when(metricsService.getRecentRamUsage("KCiemiegaTB16")).thenReturn(mockRamData);
        when(metricsService.getLatestPrediction("KCiemiegaTB16", "ram_usage_prediction")).thenReturn(50.0);
        when(dataDriftDetectionService.detectMemoryLeak(mockRamData)).thenReturn(false);

        anomalyDetectionScheduler.runAnomalyDetection();

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/alerts"), anyString());
        verify(selfHealingService, times(1)).executeMitigation(anyString(), eq("KCiemiegaTB16"));
    }

    @Test
    void shouldDoNothingWhenSystemIsHealthy() {
        List<Double> mockRamData = List.of(50.0, 50.1, 50.2, 50.1);
        when(metricsService.getRecentRamUsage("KCiemiegaTB16")).thenReturn(mockRamData);
        when(metricsService.getLatestPrediction("KCiemiegaTB16", "ram_usage_prediction")).thenReturn(50.1);
        when(dataDriftDetectionService.detectMemoryLeak(mockRamData)).thenReturn(false);

        anomalyDetectionScheduler.runAnomalyDetection();

        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
        verify(selfHealingService, never()).executeMitigation(anyString(), anyString());
    }

    @Test
    void shouldReturnEarlyWhenRecentRamUsageIsEmpty() {
        when(metricsService.getRecentRamUsage("KCiemiegaTB16")).thenReturn(Collections.emptyList());

        anomalyDetectionScheduler.runAnomalyDetection();

        verify(metricsService, never()).getLatestPrediction(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void shouldFallbackToActualCurrentRamWhenPredictionIsNull() {
        List<Double> mockRamData = List.of(50.0, 50.1, 50.2, 50.1);
        when(metricsService.getRecentRamUsage("KCiemiegaTB16")).thenReturn(mockRamData);
        when(metricsService.getLatestPrediction("KCiemiegaTB16", "ram_usage_prediction")).thenReturn(null);
        when(dataDriftDetectionService.detectMemoryLeak(mockRamData)).thenReturn(false);

        anomalyDetectionScheduler.runAnomalyDetection();

        verify(dataDriftDetectionService, times(1)).detectMemoryLeak(mockRamData);
    }

    @Test
    void shouldTriggerAlertWhenMemoryLeakIsDetectedWithoutAnomalousJump() {
        List<Double> mockRamData = List.of(50.0, 51.0, 52.0, 53.0);
        when(metricsService.getRecentRamUsage("KCiemiegaTB16")).thenReturn(mockRamData);
        when(metricsService.getLatestPrediction("KCiemiegaTB16", "ram_usage_prediction")).thenReturn(53.0);
        when(dataDriftDetectionService.detectMemoryLeak(mockRamData)).thenReturn(true);

        anomalyDetectionScheduler.runAnomalyDetection();

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/alerts"), anyString());
        verify(selfHealingService, times(1)).executeMitigation(anyString(), eq("KCiemiegaTB16"));
    }
}