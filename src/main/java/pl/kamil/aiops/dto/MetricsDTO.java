package pl.kamil.aiops.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDTO {
    private String hostname;
    private Double cpuUsage;
    private Double ramUsage;
}