package com.uptimemonitor.uptime_monitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class HttpCheckerService {

    public record CheckResult(boolean isUp, int statusCode, long responseTimeMs, String errorMessage) {}

    public CheckResult checkUrl(String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(factory);

        long start = System.currentTimeMillis();
        try {
            var response = restTemplate.getForEntity(url, Void.class);
            long responseTimeMs = System.currentTimeMillis() - start;
            int statusCode = response.getStatusCode().value();
            boolean isUp = statusCode < 400;
            log.debug("Check {} → {} {}ms", url, statusCode, responseTimeMs);
            return new CheckResult(isUp, statusCode, responseTimeMs, null);
        } catch (RestClientException e) {
            long responseTimeMs = System.currentTimeMillis() - start;
            log.debug("Check {} → error: {}", url, e.getMessage());
            return new CheckResult(false, 0, responseTimeMs, e.getMessage());
        } catch (Exception e) {
            long responseTimeMs = System.currentTimeMillis() - start;
            log.debug("Check {} → unexpected error: {}", url, e.getMessage());
            return new CheckResult(false, 0, responseTimeMs, e.getMessage());
        }
    }
}
