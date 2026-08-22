package com.estebangitpro.portfolio.adapter.in.web;

import com.estebangitpro.portfolio.adapter.in.web.mapper.AnalyticsMapperImpl;
import com.estebangitpro.portfolio.core.application.port.in.AnalyticsQueryUseCase;
import com.estebangitpro.portfolio.core.application.port.in.AnalyticsSummary;
import com.estebangitpro.portfolio.core.application.port.in.AnalyticsTrackUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract test for the analytics dashboard endpoints: the region distribution the
 * service builds must reach the HTTP response as {@code viewsByRegion} with its
 * entries intact — through the real MapStruct mapper and the real JSON serializer.
 */
@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsMapperImpl.class)
class AnalyticsControllerWebTest {

    private static final String SLUG = "mi-portfolio-web";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsTrackUseCase trackUseCase;

    @MockitoBean
    private AnalyticsQueryUseCase queryUseCase;

    @Test
    void dashboard_json_exposes_viewsByRegion_with_its_entries() throws Exception {
        given(queryUseCase.getDashboard()).willReturn(summaryWithRegions());

        mockMvc.perform(get("/api/admin/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewsByRegion.Antioquia").value(7))
                .andExpect(jsonPath("$.viewsByRegion['Valle del Cauca']").value(3));
    }

    @Test
    void project_stats_json_exposes_viewsByRegion_with_its_entries() throws Exception {
        given(queryUseCase.getProjectStats(SLUG)).willReturn(summaryWithRegions());

        mockMvc.perform(get("/api/admin/analytics/projects/{slug}", SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewsByRegion.Antioquia").value(7))
                .andExpect(jsonPath("$.viewsByRegion['Valle del Cauca']").value(3));
    }

    /** Regions carry keys with spaces on purpose: they must survive verbatim. */
    private static AnalyticsSummary summaryWithRegions() {
        Map<String, Long> viewsByProject = new LinkedHashMap<>();
        viewsByProject.put(SLUG, 10L);

        return new AnalyticsSummary(
                10, 10,
                viewsByProject,
                Map.of("2026-08-20", 10L),
                Map.of("Colombia", 10L),
                regions(),
                Map.of("desktop", 10L),
                Map.of("Mac OS", 10L),
                Map.of("Chrome", 10L),
                Map.of("America/Bogota", 10L)
        );
    }

    private static Map<String, Long> regions() {
        Map<String, Long> regions = new LinkedHashMap<>();
        regions.put("Antioquia", 7L);
        regions.put("Valle del Cauca", 3L);
        return regions;
    }
}
