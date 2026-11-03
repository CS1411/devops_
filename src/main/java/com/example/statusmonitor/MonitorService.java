package com.example.statusmonitor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Checks each configured URL and reports whether it is up. */
@Service
public class MonitorService {

    public record UrlStatus(String url, boolean up, int httpCode, long latencyMs, String error) {}

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final List<String> urls;
    private final ToIntFunction<String> probe; // returns the HTTP status code, throws on network error

    @Autowired
    public MonitorService(@Value("${monitor.urls}") String urls) {
        this(urls, MonitorService::httpGet);
    }

    MonitorService(String urls, ToIntFunction<String> probe) {
        this.urls = parseUrls(urls);
        this.probe = probe;
    }

    static List<String> parseUrls(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<UrlStatus> checkAll() {
        return urls.parallelStream().map(this::check).toList();
    }

    UrlStatus check(String url) {
        long start = System.currentTimeMillis();
        try {
            int code = probe.applyAsInt(url);
            return new UrlStatus(url, code < 400, code, System.currentTimeMillis() - start, null);
        } catch (RuntimeException e) {
            return new UrlStatus(url, false, 0, System.currentTimeMillis() - start, e.getMessage());
        }
    }

    private static int httpGet(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            return HTTP.send(req, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
