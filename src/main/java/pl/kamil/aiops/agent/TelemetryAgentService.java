package pl.kamil.aiops.agent;

import pl.kamil.aiops.dto.MetricsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class TelemetryAgentService {

    private final RestTemplate restTemplate;
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;

    private final Queue<MetricsDTO> metricsBuffer = new ConcurrentLinkedQueue<>();

    @Value("${backend.api.url:http://localhost:8080/api/metrics}")
    private String backendUrl;

    private String hostname;
    private long[] oldTicks;

    public TelemetryAgentService() {
        this.restTemplate = new RestTemplate();
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.oldTicks = hardware.getProcessor().getSystemCpuLoadTicks();

        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.hostname = "unknown-host";
        }
    }

    @Scheduled(fixedRate = 1000)
    public void collectAndSendMetrics() {
        CentralProcessor processor = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();

        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(oldTicks) * 100;
        oldTicks = processor.getSystemCpuLoadTicks();

        double totalRam = memory.getTotal();
        double usedRam = totalRam - memory.getAvailable();
        double ramUsage = (usedRam / totalRam) * 100;

        MetricsDTO currentMetric = new MetricsDTO(hostname, cpuUsage, ramUsage);
        metricsBuffer.offer(currentMetric);
        flushBuffer();
    }

    private void flushBuffer() {
        while (!metricsBuffer.isEmpty()) {
            MetricsDTO metric = metricsBuffer.peek();
            try {
                restTemplate.postForEntity(backendUrl, metric, String.class);
                metricsBuffer.poll();
                log.debug("Successfully sent metric for host: {}", metric.getHostname());
            } catch (RestClientException e) {
                log.warn("Backend is unreachable. Metric buffered. Buffer size: {}", metricsBuffer.size());
                break;
            }
        }
    }
}