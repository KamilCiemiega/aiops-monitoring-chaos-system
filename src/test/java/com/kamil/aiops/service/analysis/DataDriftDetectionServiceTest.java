package com.kamil.aiops.service.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DataDriftDetectionServiceTest {

    private DataDriftDetectionService dataDriftDetectionService;

    @BeforeEach
    void setUp() {
        dataDriftDetectionService = new DataDriftDetectionService();
    }

    @Test
    void shouldReturnTrueWhenMemoryIsConstantlyRising() {
        List<Double> leakingRamData = List.of(50.0, 52.0, 54.0, 56.0, 58.0, 60.0);

        boolean result = dataDriftDetectionService.detectMemoryLeak(leakingRamData);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenMemoryIsStable() {
        List<Double> stableRamData = List.of(50.0, 50.2, 49.9, 50.1, 50.0, 50.1);

        boolean result = dataDriftDetectionService.detectMemoryLeak(stableRamData);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenDataIsInsufficient() {
        List<Double> emptyData = Collections.emptyList();
        List<Double> tooShortData = List.of(50.0);

        assertFalse(dataDriftDetectionService.detectMemoryLeak(emptyData));
        assertFalse(dataDriftDetectionService.detectMemoryLeak(tooShortData));
    }

    @Test
    void shouldReturnFalseWhenRamUsageHistoryIsNull() {
        assertFalse(dataDriftDetectionService.detectMemoryLeak(null));
    }

    @Test
    void shouldReturnFalseWhenDenominatorIsZeroDueToNumericalOverflow() {
        List<Double> overflowData = List.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        boolean result = dataDriftDetectionService.detectMemoryLeak(overflowData);

        assertFalse(result);
    }
}