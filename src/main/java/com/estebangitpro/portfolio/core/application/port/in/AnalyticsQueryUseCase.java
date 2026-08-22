package com.estebangitpro.portfolio.core.application.port.in;

import com.estebangitpro.portfolio.core.application.port.in.AnalyticsSummary;

public interface AnalyticsQueryUseCase {
    AnalyticsSummary getDashboard();
    AnalyticsSummary getProjectStats(String slug);
}
