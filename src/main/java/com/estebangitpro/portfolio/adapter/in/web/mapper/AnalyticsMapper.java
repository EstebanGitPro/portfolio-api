package com.estebangitpro.portfolio.adapter.in.web.mapper;

import com.estebangitpro.portfolio.core.application.port.in.AnalyticsSummary;
import com.estebangitpro.portfolio.adapter.in.web.dto.AnalyticsResponse;
import org.mapstruct.Mapper;

/**
 * Translates the analytics domain summary into its HTTP representation.
 */
@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    AnalyticsResponse toResponse(AnalyticsSummary summary);
}
