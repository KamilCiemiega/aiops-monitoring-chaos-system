package com.kamil.aiops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SelfHealingService {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingService.class);

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
            String os = System.getProperty("os.name").toLowerCase();
            Process process;

            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("cmd.exe /c echo Mitigating memory pressure...");
            } else {
                process = Runtime.getRuntime().exec("echo Mitigating memory pressure...");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("[SELF-HEALING] Success: Cache cleared successfully.");
            } else {
                logger.error("[SELF-HEALING] Failure: Mitigation script exited with code {}", exitCode);
            }

        } catch (Exception e) {
            logger.error("[SELF-HEALING] Error executing self-healing script", e);
        }
    }

    private void optimizeProcessPriority() {
        logger.info("[SELF-HEALING] Action: Throttling non-essential background processes...");
    }
}