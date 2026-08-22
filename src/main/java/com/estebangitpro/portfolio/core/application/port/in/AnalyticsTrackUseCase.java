package com.estebangitpro.portfolio.core.application.port.in;

import com.estebangitpro.portfolio.core.domain.AnalyticsType;

public interface AnalyticsTrackUseCase {
    void track(AnalyticsType type, String path, String projectSlug,
               String referrer, String userAgent, String ip, String sessionId);
}
