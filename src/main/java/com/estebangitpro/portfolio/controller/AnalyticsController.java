package com.estebangitpro.portfolio.controller;

import com.estebangitpro.portfolio.dto.AnalyticsTrackRequest;

import com.estebangitpro.portfolio.model.AnalyticsType;
import com.estebangitpro.portfolio.service.AnalyticsQueryUseCase;
import com.estebangitpro.portfolio.service.AnalyticsTrackUseCase;
import com.estebangitpro.portfolio.dto.AnalyticsResponse;
import com.estebangitpro.portfolio.mapper.AnalyticsMapper;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Analytics", description = "Registro de visitas y dashboard")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsTrackUseCase trackUseCase;
    private final AnalyticsQueryUseCase queryUseCase;
    private final AnalyticsMapper mapper;
    private final ClientIpResolver clientIpResolver;

    @Operation(summary = "Registrar una visita",
            description = "El servidor deriva referrer, User-Agent e IP de las cabeceras. La IP se hashea con SHA-256 antes de guardarse. Los eventos expiran a los 90 dias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visita registrada")
    })
    @PostMapping("/analytics/track")
    public ResponseEntity<Void> track(@RequestBody AnalyticsTrackRequest request,
                                      HttpServletRequest httpRequest) {
        String referrer = httpRequest.getHeader("Referer");
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = clientIpResolver.resolve(httpRequest);

        AnalyticsType type = AnalyticsType.valueOf(request.type());
        trackUseCase.track(type, request.path(), request.projectSlug(),
                referrer, userAgent, ip, request.sessionId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Dashboard de los ultimos 30 dias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metricas agregadas")
    })
    @GetMapping("/admin/analytics")
    public ResponseEntity<AnalyticsResponse> getDashboard() {
        return ResponseEntity.ok(mapper.toResponse(queryUseCase.getDashboard()));
    }

    @Operation(summary = "Metricas de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metricas del proyecto")
    })
    @GetMapping("/admin/analytics/projects/{slug}")
    public ResponseEntity<AnalyticsResponse> getProjectStats(@PathVariable String slug) {
        return ResponseEntity.ok(mapper.toResponse(queryUseCase.getProjectStats(slug)));
    }
}
