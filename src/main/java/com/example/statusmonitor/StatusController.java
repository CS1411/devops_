package com.example.statusmonitor;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final MonitorService monitor;

    public StatusController(MonitorService monitor) {
        this.monitor = monitor;
    }

    /** Liveness/readiness endpoint. Never calls external URLs, so a slow site can't fail the probe. */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "servedBy", hostname(), "monitoredUrls", monitor.getUrls().size());
    }

    /** Checks every configured URL right now. */
    @GetMapping("/status")
    public List<MonitorService.UrlStatus> status() {
        return monitor.checkAll();
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
