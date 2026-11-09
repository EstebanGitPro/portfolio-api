package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.model.AnalyticsType;

public interface AnalyticsTrackUseCase {
    void track(AnalyticsType type, String path, String projectSlug,
               String referrer, String userAgent, String ip, String sessionId);
}
