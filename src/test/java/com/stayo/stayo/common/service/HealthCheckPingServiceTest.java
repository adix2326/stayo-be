package com.stayo.stayo.common.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HealthCheckPingServiceTest {

    @Test
    void testPingHealthEndpointGracefulFailure() {
        // Construct the service with a dummy URL to verify it handles connection failures gracefully
        HealthCheckPingService healthCheckPingService = new HealthCheckPingService("http://localhost:9999/health");

        // The method should catch exceptions internally and log them, rather than throwing them
        assertDoesNotThrow(() -> healthCheckPingService.pingHealthEndpoint());
    }
}
