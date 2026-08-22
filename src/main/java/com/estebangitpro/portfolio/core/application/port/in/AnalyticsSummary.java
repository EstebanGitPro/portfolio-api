package com.estebangitpro.portfolio.core.application.port.in;

import java.util.Map;

/**
 * Aggregated analytics figures for a period, expressed in domain terms.
 *
 * <p>Each distribution maps a dimension value (project slug, day, country, ...)
 * to the number of recorded views.
 */
public record AnalyticsSummary(
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
