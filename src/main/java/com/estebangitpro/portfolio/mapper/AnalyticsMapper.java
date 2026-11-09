package com.estebangitpro.portfolio.mapper;

import com.estebangitpro.portfolio.dto.AnalyticsSummary;
import com.estebangitpro.portfolio.dto.AnalyticsResponse;
import org.mapstruct.Mapper;

/**
 * Translates the analytics domain summary into its HTTP representation.
 */
@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    AnalyticsResponse toResponse(AnalyticsSummary summary);
}
