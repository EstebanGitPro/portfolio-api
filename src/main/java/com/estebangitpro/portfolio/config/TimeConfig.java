package com.estebangitpro.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Exposes the clock the application reads the current time from.
 *
 * <p>Injecting it instead of calling {@code LocalDateTime.now()} keeps time an explicit
 * dependency, so services can be tested against a fixed instant. UTC is used so stored
 * timestamps do not shift with the host's timezone.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
