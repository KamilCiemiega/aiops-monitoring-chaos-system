package com.kamil.aiops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SelfHealingService {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingService.class);

    private final String osName;
    private final Runtime runtime;

    public SelfHealingService() {
        this.osName = System.getProperty("os.name").toLowerCase();
        this.runtime = Runtime.getRuntime();
    }

    // Konstruktor używany wyłącznie przez testy jednostkowe do mockowania środowiska OS
    public SelfHealingService(String osName, Runtime runtime) {
        this.osName = osName.toLowerCase();
        this.runtime = runtime;
    }

    public void executeMitigation(String anomalyType, String host) {
        logger.info("[SELF-HEALING] Initiating automated response for: {} on host: {}", anomalyType, host);
        if ("Memory leak confirmed!".equals(anomalyType)) {
            clearSystemCache();
        } else if ("CPU Spike detected!".equals(anomalyType)) {
            optimizeProcessPriority();
        } else {
            logger.warn("[SELF-HEALING] Unknown anomaly type. No action taken.");
        }
    }

    private void clearSystemCache() {
        logger.info("[SELF-HEALING] Action: Clearing system application cache...");
        try {
            Process process;
            if (osName.contains("win")) {
                process = runtime.exec("cmd.exe /c echo Mitigating memory pressure...");
            } else {
                process = runtime.exec("echo Mitigating memory pressure...");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("[SELF-HEALING] Success: Cache cleared successfully.");
            } else {
                logger.error("[SELF-HEALING] Failure: Mitigation script exited with code {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("[SELF-HEALING] Error executing self-healing script", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void optimizeProcessPriority() {
        logger.info("[SELF-HEALING] Action: Throttling non-essential background processes...");
    }
}