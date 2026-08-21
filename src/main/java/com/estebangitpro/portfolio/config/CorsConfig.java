package com.estebangitpro.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Local development origins are always allowed on any port: {@code flutter run -d chrome}
     * serves the app from a random port, so exact ports cannot be enumerated. Only pages
     * running on the developer's own machine can match these patterns.
     */
    private static final List<String> LOCAL_DEV_ORIGIN_PATTERNS =
            List.of("http://localhost:[*]", "http://127.0.0.1:[*]");

    /**
     * Real front-end origins come from {@code app.cors.allowed-origins}: in production this is
     * set through the mounted application-prod.yaml. A wildcard combined with credentials lets
     * any site on the internet call the API as the visitor, so production lists exact origins.
     */
    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> originPatterns = new ArrayList<>(allowedOrigins);
                originPatterns.addAll(LOCAL_DEV_ORIGIN_PATTERNS);
                registry.addMapping("/**")
                        .allowedOriginPatterns(originPatterns.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
