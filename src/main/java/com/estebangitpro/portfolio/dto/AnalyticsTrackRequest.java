package com.estebangitpro.portfolio.dto;

public record AnalyticsTrackRequest(
        String type,
        String path,
        String projectSlug,
        String sessionId
) {
}
