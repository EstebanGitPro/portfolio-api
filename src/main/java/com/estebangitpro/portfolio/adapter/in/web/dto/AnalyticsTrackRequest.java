package com.estebangitpro.portfolio.adapter.in.web.dto;

public record AnalyticsTrackRequest(
        String type,
        String path,
        String projectSlug,
        String sessionId
) {
}
