package com.estebangitpro.portfolio.adapter.out.persistence;

import com.estebangitpro.portfolio.core.domain.AnalyticsType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "analytics")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AnalyticsMongoDocument {
    @Id
    private String id;
    private AnalyticsType type;
    private String path;
    private String projectSlug;
    private String referrer;
    private String userAgent;
    private String ipHash;
    private String country;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String deviceType;
    private String operatingSystem;
    private String browser;
    private String sessionId;
    @CreatedDate
    private LocalDateTime createdAt;
}
