package com.kamil.aiops.controller;

import com.kamil.aiops.dto.MetricsDTO;
import com.kamil.aiops.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing system metrics
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Creates and saves new system metrics
     * @param metricsDTO metrics data from request body
     * @return ResponseEntity with "OK" status
     */
    @PostMapping
    public ResponseEntity<String> saveMetrics(@RequestBody MetricsDTO metricsDTO) {
        metricsService.saveMetric(metricsDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}

