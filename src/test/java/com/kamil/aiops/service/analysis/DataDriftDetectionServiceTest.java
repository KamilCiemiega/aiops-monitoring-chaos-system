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
    void shouldReturnFalseWhenRamUsageHistoryIsNull() {
        assertFalse(dataDriftDetectionService.detectMemoryLeak(null));
    }

    @Test
    void shouldReturnFalseWhenDataIsLessThanTwoElements() {
        assertFalse(dataDriftDetectionService.detectMemoryLeak(Collections.emptyList()));
        assertFalse(dataDriftDetectionService.detectMemoryLeak(List.of(50.0)));
    }

    @Test
    void shouldReturnFalseWhenDenominatorIsZero() {
        List<Double> identicalPointsWithZeroVariance = new java.util.ArrayList<>() {
            @Override
            public int size() {
                return 5;
            }
            @Override
            public Double get(int index) {
                return 50.0;
            }
        };
        boolean result = dataDriftDetectionService.detectMemoryLeak(identicalPointsWithZeroVariance);
        assertFalse(result);
    }
}