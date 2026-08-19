package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.domain.Analytics;
import com.estebangitpro.portfolio.core.application.port.out.AnalyticsRepository;
import com.estebangitpro.portfolio.core.domain.AnalyticsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalyticsMongoAdapter implements AnalyticsRepository {

    private final AnalyticsMongoRepository mongoRepository;

    @Override
    public Analytics save(Analytics analytics) {
        AnalyticsMongoDocument doc = toDocument(analytics);
        AnalyticsMongoDocument saved = mongoRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return mongoRepository.countByCreatedAtBetween(start, end);
    }

    @Override
    public long countByType(AnalyticsType type) {
        return mongoRepository.countByType(type);
    }

    @Override
    public long countByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end) {
        return mongoRepository.countByProjectSlugAndCreatedAtBetween(slug, start, end);
    }

    @Override
    public List<Analytics> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return mongoRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Analytics> findByProjectSlugAndCreatedAtBetween(String slug, LocalDateTime start, LocalDateTime end) {
        return mongoRepository.findByProjectSlugAndCreatedAtBetween(slug, start, end)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AnalyticsMongoDocument toDocument(Analytics domain) {
        return AnalyticsMongoDocument.builder()
                .id(domain.getId())
                .type(domain.getType())
                .path(domain.getPath())
                .projectSlug(domain.getProjectSlug())
                .referrer(domain.getReferrer())
                .userAgent(domain.getUserAgent())
                .ipHash(domain.getIpHash())
                .country(domain.getCountry())
                .region(domain.getRegion())
                .city(domain.getCity())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .timezone(domain.getTimezone())
                .deviceType(domain.getDeviceType())
                .operatingSystem(domain.getOperatingSystem())
                .browser(domain.getBrowser())
                .sessionId(domain.getSessionId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private Analytics toDomain(AnalyticsMongoDocument doc) {
        return new Analytics(
                doc.getId(), doc.getType(), doc.getPath(), doc.getProjectSlug(),
                doc.getReferrer(), doc.getUserAgent(), doc.getIpHash(),
                doc.getCountry(), doc.getRegion(), doc.getCity(),
                doc.getLatitude(), doc.getLongitude(), doc.getTimezone(),
                doc.getDeviceType(), doc.getOperatingSystem(), doc.getBrowser(),
                doc.getSessionId(), doc.getCreatedAt()
        );
    }
}
