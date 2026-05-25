package com.kamil.aiops.service;

import com.kamil.aiops.dto.MetricsDTO;
import com.kamil.aiops.entity.SystemMetrics;
import com.kamil.aiops.repository.SystemMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service class for managing system metrics
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final SystemMetricsRepository metricsRepository;

    /**
     * Saves metrics from DTO to database
     * @param dto MetricsDTO containing metric data
     * @return saved SystemMetrics entity
     */
    public SystemMetrics saveMetric(MetricsDTO dto) {
        // Convert DTO to Entity
        SystemMetrics metrics = new SystemMetrics();
        metrics.setHostname(dto.getHostname());
        metrics.setCpuUsage(dto.getCpuUsage());
        metrics.setRamUsage(dto.getRamUsage());

        // Set current timestamp
        metrics.setTimestamp(LocalDateTime.now());

        // Save to database using repository
        return metricsRepository.save(metrics);
    }
}

