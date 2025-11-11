package com.devops.service2;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class StatusController {
    private static final String STORAGE_URL = "http://storage:8080/log";

    @GetMapping("/status")
    public String status() {
        String record = analyzeState();
        logToStorage(record);
        return record;
    }

    private String analyzeState() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        double uptimeHours = uptimeMillis / 1000.0 / 3600.0;
        long freeDiskMB = 0;
        try {
            freeDiskMB = Files.getFileStore(Paths.get("/")).getUsableSpace() / (1024 * 1024);
        } catch (IOException e) {
            // ignore
        }

        // Add ISO 8601 timestamp in UTC
        String timestamp = java.time.Instant.now().toString();
        return String.format("%s: uptime %.2f hours, free disk in root: %d MBytes",
                timestamp, uptimeHours, freeDiskMB);
    }

    private void logToStorage(String record) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(STORAGE_URL, record, String.class);
        } catch (Exception e) {
            // ignore
        }
    }
}
