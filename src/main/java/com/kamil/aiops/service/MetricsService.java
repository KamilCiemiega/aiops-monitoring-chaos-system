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
        List<FluxTable> tables = queryApi.query(fluxQuery, organization);
        List<Double> ramValues = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                ramValues.add((Double) record.getValueByKey("_value"));
            }
        }
        log.debug("Fetched {} RAM samples from InfluxDB for host: {}", ramValues.size(), hostname);
        return ramValues;
    }

    public List<Double> getRecentCpuUsage(String hostname) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"system_metrics\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"cpu_usage\") " +
                        "|> filter(fn: (r) => r[\"hostname\"] == \"%s\") " +
                        "|> sort(columns: [\"_time\"], desc: true) " +
                        "|> limit(n: 50) " +
                        "|> sort(columns: [\"_time\"], desc: false)",
                bucket, hostname
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, organization);
        List<Double> cpuValues = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                cpuValues.add((Double) record.getValueByKey("_value"));
            }
        }
        log.debug("Fetched {} CPU samples from InfluxDB for forecasting for host: {}", cpuValues.size(), hostname);
        return cpuValues;
    }

    public void savePredictionMetric(String hostname, String fieldName, Double value, Instant futureTime) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("system_metrics")
                .addTag("hostname", hostname)
                .addTag("datatype", "prediction")
                .addField(fieldName, value)
                .time(futureTime, WritePrecision.MS);

        try {
            writeApi.writePoint(bucket, organization, point);
            log.debug("Prediction metric [{}] written to InfluxDB for host: {}", fieldName, hostname);
        } catch (Exception e) {
            log.error("Error writing prediction to InfluxDB: {}", e.getMessage());
        }
    }

    public Double getLatestPrediction(String hostname, String fieldName) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -5m, stop: 5m) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"system_metrics\") " +
                        "|> filter(fn: (r) => r[\"datatype\"] == \"prediction\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"滑_field_placeholder\") " +
                        "|> filter(fn: (r) => r[\"hostname\"] == \"%s\") " +
                        "|> last()",
                bucket, hostname
        );

        fluxQuery = fluxQuery.replace("滑_field_placeholder", fieldName);

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, organization);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object val = record.getValueByKey("_value");
                    if (val instanceof Double) {
                        return (Double) val;
                    } else if (val instanceof Number) {
                        return ((Number) val).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching prediction from InfluxDB: {}", e.getMessage());
        }
        return null;
    }
}