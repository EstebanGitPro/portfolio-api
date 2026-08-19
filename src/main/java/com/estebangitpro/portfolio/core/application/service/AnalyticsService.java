package com.estebangitpro.portfolio.core.application.service;

import com.estebangitpro.portfolio.core.application.port.in.AnalyticsSummary;
import com.estebangitpro.portfolio.core.domain.Analytics;
import com.estebangitpro.portfolio.core.domain.AnalyticsType;
import com.estebangitpro.portfolio.core.application.port.out.AnalyticsRepository;
import com.estebangitpro.portfolio.core.application.port.out.DeviceParser;
import com.estebangitpro.portfolio.core.application.port.out.GeoResolver;
import com.estebangitpro.portfolio.core.application.port.in.AnalyticsQueryUseCase;
import com.estebangitpro.portfolio.core.application.port.in.AnalyticsTrackUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService implements AnalyticsTrackUseCase, AnalyticsQueryUseCase {

    private final AnalyticsRepository repository;
    private final Clock clock;
    private final GeoResolver geoResolver;
    private final DeviceParser deviceParser;

    @Override
    public void track(AnalyticsType type, String path, String projectSlug,
                      String referrer, String userAgent, String ip, String sessionId) {

        DeviceParser.DeviceInfo deviceInfo = deviceParser.parse(userAgent);
        GeoResolver.GeoInfo geoInfo = geoResolver.resolve(ip);

        Analytics analytics = new Analytics(
                null, type, path, projectSlug,
                referrer, userAgent, hashIp(ip),
                geoInfo.country(), geoInfo.region(), geoInfo.city(),
                geoInfo.latitude(), geoInfo.longitude(), geoInfo.timezone(),
                deviceInfo.deviceType(), deviceInfo.operatingSystem(), deviceInfo.browser(),
                sessionId, LocalDateTime.now(clock)
        );

        repository.save(analytics);
    }

    @Override
    public AnalyticsSummary getDashboard() {
        LocalDateTime end = LocalDateTime.now(clock);
        LocalDateTime start = end.minusDays(30);

        long totalViews = repository.countByCreatedAtBetween(start, end);
        long projectViews = repository.countByType(AnalyticsType.PROJECT_VIEW);
        List<Analytics> analyticsList = repository.findByCreatedAtBetween(start, end);

        return buildSummary(totalViews, projectViews, analyticsList);
    }

    @Override
    public AnalyticsSummary getProjectStats(String slug) {
        LocalDateTime end = LocalDateTime.now(clock);
        LocalDateTime start = end.minusDays(30);

        long totalViews = repository.countByProjectSlugAndCreatedAtBetween(slug, start, end);
        List<Analytics> analyticsList = repository.findByProjectSlugAndCreatedAtBetween(slug, start, end);

        return buildSummary(totalViews, totalViews, analyticsList);
    }

    private AnalyticsSummary buildSummary(long totalViews, long projectViews, List<Analytics> analyticsList) {
        return new AnalyticsSummary(
                totalViews,
                projectViews,
                buildDistribution(analyticsList, Analytics::getProjectSlug),
                buildViewsByDay(analyticsList),
                buildDistribution(analyticsList, Analytics::getCountry),
                buildDistribution(analyticsList, Analytics::getDeviceType),
                buildDistribution(analyticsList, Analytics::getOperatingSystem),
                buildDistribution(analyticsList, Analytics::getBrowser),
                buildDistribution(analyticsList, Analytics::getTimezone)
        );
    }

    private String hashIp(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private Map<String, Long> buildDistribution(List<Analytics> list,
                                                java.util.function.Function<Analytics, String> classifier) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Analytics a : list) {
            String key = classifier.apply(a);
            if (key != null) {
                result.merge(key, 1L, Long::sum);
            }
        }
        return result;
    }

    private Map<String, Long> buildViewsByDay(List<Analytics> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> result = new LinkedHashMap<>();
        for (Analytics a : list) {
            String day = a.getCreatedAt().format(formatter);
            result.merge(day, 1L, Long::sum);
        }
        return result;
    }
}
