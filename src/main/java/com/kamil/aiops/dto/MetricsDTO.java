package com.kamil.aiops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming metrics data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDTO {

    private String hostname;
    private Double cpuUsage;
    private Double ramUsage;
}

