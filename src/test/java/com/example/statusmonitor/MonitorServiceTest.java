package com.example.statusmonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Unit tests with a fake probe, so no test touches the network. */
class MonitorServiceTest {

    @Test
    void parsesCommaSeparatedUrls() {
        assertEquals(List.of("https://a.com", "https://b.com"),
                MonitorService.parseUrls("https://a.com,https://b.com"));
    }

    @Test
    void trimsSpacesAndSkipsBlankEntries() {
        assertEquals(List.of("https://a.com", "https://b.com"),
                MonitorService.parseUrls(" https://a.com , ,https://b.com,"));
    }

    @Test
    void urlIsUpFor2xxResponse() {
        MonitorService svc = new MonitorService("https://a.com", url -> 200);
        MonitorService.UrlStatus s = svc.check("https://a.com");
        assertTrue(s.up());
        assertEquals(201, s.httpCode()); // deliberately wrong: CI safety-net demo
    }

    @Test
    void urlIsDownFor5xxResponse() {
        MonitorService svc = new MonitorService("https://a.com", url -> 503);
        assertFalse(svc.check("https://a.com").up());
    }

    @Test
    void urlIsDownWhenProbeThrows() {
        MonitorService svc = new MonitorService("https://a.com", url -> {
            throw new IllegalStateException("UnknownHostException: a.com");
        });
        MonitorService.UrlStatus s = svc.check("https://a.com");
        assertFalse(s.up());
        assertEquals(0, s.httpCode());
        assertEquals("UnknownHostException: a.com", s.error());
    }

    @Test
    void checkAllReturnsOneResultPerUrl() {
        MonitorService svc = new MonitorService("https://up.com,https://down.com",
                url -> url.contains("up") ? 200 : 500);
        List<MonitorService.UrlStatus> results = svc.checkAll();
        assertEquals(2, results.size());
        assertEquals(1, results.stream().filter(MonitorService.UrlStatus::up).count());
    }
}
