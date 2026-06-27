package com.kamil.aiops.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfHealingServiceTest {

    @Mock
    private Runtime mockRuntime;

    @Mock
    private Process mockProcess;

    @Test
    void shouldExecuteMitigationForWindowsSuccess() throws Exception {
        when(mockRuntime.exec("cmd.exe /c echo Mitigating memory pressure...")).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenReturn(0);

        SelfHealingService service = new SelfHealingService("windows", mockRuntime);
        assertDoesNotThrow(() -> service.executeMitigation("Memory leak confirmed!", "host1"));
    }

    @Test
    void shouldExecuteMitigationForLinuxFailure() throws Exception {
        when(mockRuntime.exec("echo Mitigating memory pressure...")).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenReturn(1);

        SelfHealingService service = new SelfHealingService("linux", mockRuntime);
        assertDoesNotThrow(() -> service.executeMitigation("Memory leak confirmed!", "host1"));
    }

    @Test
    void shouldHandleExceptionInClearSystemCache() throws Exception {
        when(mockRuntime.exec(anyString())).thenThrow(new IOException("Simulated OS error"));

        SelfHealingService service = new SelfHealingService("windows", mockRuntime);
        assertDoesNotThrow(() -> service.executeMitigation("Memory leak confirmed!", "host1"));
    }

    @Test
    void shouldHandleInterruptedExceptionInClearSystemCache() throws Exception {
        when(mockRuntime.exec(anyString())).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenThrow(new InterruptedException("Simulated interruption"));

        SelfHealingService service = new SelfHealingService("windows", mockRuntime);
        assertDoesNotThrow(() -> service.executeMitigation("Memory leak confirmed!", "host1"));
    }

    @Test
    void shouldExecuteMitigationForCpuSpike() {
        SelfHealingService service = new SelfHealingService();
        assertDoesNotThrow(() -> service.executeMitigation("CPU Spike detected!", "host1"));
    }

    @Test
    void shouldHandleUnknownAnomalyGracefully() {
        SelfHealingService service = new SelfHealingService();
        assertDoesNotThrow(() -> service.executeMitigation("Unknown Error", "host1"));
    }
}