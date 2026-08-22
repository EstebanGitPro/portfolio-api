package com.estebangitpro.portfolio.core.domain;

import java.time.LocalDateTime;

public class Analytics {
    private final String id;
    private final AnalyticsType type;
    private final String path;
    private final String projectSlug;
    private final String referrer;
    private final String userAgent;
    private final String ipHash;
    private final String country;
    private final String region;
    private final String city;
    private final Double latitude;
    private final Double longitude;
    private final String timezone;
    private final String deviceType;
    private final String operatingSystem;
    private final String browser;
    private final String sessionId;
    private final LocalDateTime createdAt;

    public Analytics(String id, AnalyticsType type, String path, String projectSlug,
                     String referrer, String userAgent, String ipHash,
                     String country, String region, String city,
                     Double latitude, Double longitude, String timezone,
                     String deviceType, String operatingSystem, String browser,
                     String sessionId, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.path = path;
        this.projectSlug = projectSlug;
        this.referrer = referrer;
        this.userAgent = userAgent;
        this.ipHash = ipHash;
        this.country = country;
        this.region = region;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.deviceType = deviceType;
        this.operatingSystem = operatingSystem;
        this.browser = browser;
        this.sessionId = sessionId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public AnalyticsType getType() { return type; }
    public String getPath() { return path; }
    public String getProjectSlug() { return projectSlug; }
    public String getReferrer() { return referrer; }
    public String getUserAgent() { return userAgent; }
    public String getIpHash() { return ipHash; }
    public String getCountry() { return country; }
    public String getRegion() { return region; }
    public String getCity() { return city; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getTimezone() { return timezone; }
    public String getDeviceType() { return deviceType; }
    public String getOperatingSystem() { return operatingSystem; }
    public String getBrowser() { return browser; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
