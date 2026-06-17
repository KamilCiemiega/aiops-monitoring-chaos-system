package com.kamil.aiops.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.kamil.aiops.dto.MetricsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    // CHANGED: Renamed variable to avoid package namespace collision
    @Value("${influxdb.org}")
    private String organization;

    public void saveMetric(MetricsDTO dto) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("system_metrics")
                .addTag("hostname", dto.getHostname())
                .addField("cpu_usage", dto.getCpuUsage())
                .addField("ram_usage", dto.getRamUsage())
                .time(Instant.now(), WritePrecision.MS);

        try {
            // CHANGED: Using the renamed variable
            writeApi.writePoint(bucket, organization, point);
            log.debug("Metric written to InfluxDB for host: {}", dto.getHostname());
        } catch (Exception e) {
            log.error("Error writing to InfluxDB: {}", e.getMessage());
        }
    }

    public List<Double> getRecentRamUsage(String hostname) {

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"system_metrics\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"ram_usage\") " +
                        "|> filter(fn: (r) => r[\"hostname\"] == \"%s\") " +
                        "|> sort(columns: [\"_time\"], desc: true) " +
                        "|> limit(n: 100) " +
                        "|> sort(columns: [\"_time\"], desc: false)",
                bucket, hostname
        );

        QueryApi queryApi = influxDBClient.getQueryApi();

        // CHANGED: Using the renamed variable
        List<FluxTable> tables = queryApi.query(fluxQuery, organization);
        List<Double> ramValues = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                ramValues.add((Double) record.getValueByKey("_value"));
            }
        }

        // CHANGED: Translated the log to English
        log.debug("Fetched {} RAM samples from InfluxDB for host: {}", ramValues.size(), hostname);
        return ramValues;
    }
}