package pl.kamil.aiops.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Chaos API - Controller for controlled fault injection.
 * Used to test AIOps algorithms by simulating infrastructure issues.
 */
@Slf4j
@RestController
@RequestMapping("/api/chaos")
public class ChaosController {

    // A static list to hold memory references, preventing Garbage Collection and causing a true memory leak
    private final List<byte[]> memoryLeakBuffer = new ArrayList<>();

    // Flag to control the CPU stress loop
    private volatile boolean cpuStressActive = false;

    /**
     * Simulates a gradual memory leak.
     * Allocates the specified amount of Megabytes in RAM, which won't be freed.
     * * @param sizeMb The size of memory to leak in MB
     * @return Confirmation message
     */
    @PostMapping("/memory-leak")
    public ResponseEntity<String> injectMemoryLeak(@RequestParam(defaultValue = "50") int sizeMb) {
        log.warn("Chaos Engineering: Injecting {} MB Memory Leak...", sizeMb);

        // 1 MB = 1024 * 1024 bytes
        byte[] garbage = new byte[sizeMb * 1024 * 1024];
        memoryLeakBuffer.add(garbage);

        return ResponseEntity.ok("Memory leak injected: Added " + sizeMb + " MB. Total chunks: " + memoryLeakBuffer.size());
    }

    /**
     * Clears the memory leak buffer, allowing the Garbage Collector to free the RAM.
     * * @return Confirmation message
     */
    @PostMapping("/memory-leak/resolve")
    public ResponseEntity<String> resolveMemoryLeak() {
        log.info("Chaos Engineering: Resolving memory leak, clearing buffer...");
        memoryLeakBuffer.clear();
        System.gc(); // Suggest Garbage Collection

        return ResponseEntity.ok("Memory leak resolved. Buffer cleared.");
    }

    /**
     * Simulates a CPU overload by running infinite math calculations in multiple threads.
     * * @param durationSeconds How long the CPU stress should last
     * @return Confirmation message
     */
    @PostMapping("/cpu-stress")
    public ResponseEntity<String> injectCpuStress(@RequestParam(defaultValue = "10") int durationSeconds) {
        if (cpuStressActive) {
            return ResponseEntity.badRequest().body("CPU stress is already running!");
        }

        log.warn("Chaos Engineering: Starting CPU stress test for {} seconds...", durationSeconds);
        cpuStressActive = true;

        // Spin up threads corresponding to the number of CPU cores
        int cores = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cores; i++) {
            new Thread(() -> {
                long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
                while (cpuStressActive && System.currentTimeMillis() < endTime) {
                    // Busy-wait loop to spike CPU usage (calculate random math)
                    Math.pow(Math.random(), Math.random());
                }
            }).start();
        }

        // Automatically disable the flag after the specified duration
        new Thread(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
                cpuStressActive = false;
                log.info("Chaos Engineering: CPU stress test completed naturally.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.ok("CPU stress test started on " + cores + " cores for " + durationSeconds + " seconds.");
    }
}