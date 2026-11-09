package com.estebangitpro.portfolio.service;

import com.estebangitpro.portfolio.dto.AnalyticsSummary;

public interface AnalyticsQueryUseCase {
    AnalyticsSummary getDashboard();
    AnalyticsSummary getProjectStats(String slug);
}
