package com.estebangitpro.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

    /**
     * Used only for geolocation lookups, which happen inside the request thread of
     * {@code POST /api/analytics/track}. Without timeouts a provider that accepts the
     * connection and then stalls holds that thread until the container gives up — and a
     * tracked view is worth no thread at all. Bounded low on purpose: analytics that
     * slows the API down has its priorities backwards.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return new RestTemplate(requestFactory);
    }
}
