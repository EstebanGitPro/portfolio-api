package com.estebangitpro.portfolio.adapter.in.web.dto;

import java.util.Map;

public record AnalyticsResponse(
        long totalViews,
        long projectViews,
        Map<String, Long> viewsByProject,
        Map<String, Long> viewsByDay,
        Map<String, Long> viewsByCountry,
        Map<String, Long> viewsByRegion,
        Map<String, Long> viewsByDevice,
        Map<String, Long> viewsByOs,
        Map<String, Long> viewsByBrowser,
        Map<String, Long> viewsByTimezone
) {
}
