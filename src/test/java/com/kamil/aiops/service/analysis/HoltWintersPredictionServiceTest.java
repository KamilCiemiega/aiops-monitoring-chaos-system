package com.kamil.aiops.service.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HoltWintersPredictionServiceTest {

    private HoltWintersPredictionService holtWintersPredictionService;

    @BeforeEach
    void setUp() {
        holtWintersPredictionService = new HoltWintersPredictionService();
    }

    @Test
    void shouldReturnCorrectNumberOfPredictedPoints() {
        List<Double> cpuHistory = List.of(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0);
        int period = 4;
        int forecastLength = 5;

        List<Double> predictions = holtWintersPredictionService.predict(cpuHistory, period, forecastLength);

        assertNotNull(predictions);
        assertEquals(forecastLength, predictions.size());
        assertTrue(predictions.getFirst() > 19.0);
    }

    @Test
    void shouldReturnEmptyListWhenDataIsNull() {
        List<Double> predictions = holtWintersPredictionService.predict(null, 5, 5);
        assertNotNull(predictions);
        assertTrue(predictions.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenHistoryIsEmpty() {
        List<Double> predictions = holtWintersPredictionService.predict(Collections.emptyList(), 5, 5);
        assertNotNull(predictions);
        assertTrue(predictions.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenHistoryIsExactlyOneElementLessThanTwoPeriods() {
        List<Double> edgeCaseHistory = List.of(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0);
        int period = 4;

        List<Double> predictions = holtWintersPredictionService.predict(edgeCaseHistory, period, 5);

        assertNotNull(predictions);
        assertTrue(predictions.isEmpty());
    }
}