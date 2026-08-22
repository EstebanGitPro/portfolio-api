package com.estebangitpro.portfolio.adapter.out.geo;

import com.estebangitpro.portfolio.core.application.port.out.RegionResolver;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Fills in the one field the primary provider does not carry: the sub-national region.
 *
 * <p><strong>This call travels over plain HTTP.</strong> ip-api.com serves HTTPS only on its
 * paid tier, so the visitor's address leaves the server unencrypted. That is a deliberate,
 * accepted trade-off for this deployment — it is also why the address is never persisted in
 * the clear (see {@code AnalyticsService.hashIp}). Anything beyond the region belongs to the
 * HTTPS provider, so only {@code regionName} is requested.
 *
 * <p>The free tier allows 45 requests per minute and is licensed for non-commercial use.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpApiRegionResolver implements RegionResolver {

    /** Only {@code regionName} is requested; {@code status} distinguishes a miss from a failure. */
    private static final String IP_API_URL = "http://ip-api.com/json/%s?fields=status,regionName";

    private static final String STATUS_SUCCESS = "success";

    private final RestTemplate restTemplate;

    @Override
    public String resolve(String ip) {
        if (PrivateIpRanges.isPrivate(ip)) {
            return null;
        }

        try {
            IpApiResponse response = restTemplate.getForObject(String.format(IP_API_URL, ip), IpApiResponse.class);

            if (response == null || !STATUS_SUCCESS.equals(response.status())) {
                return null;
            }
            return response.regionName() == null || response.regionName().isBlank()
                    ? null
                    : response.regionName();
        } catch (Exception e) {
            // Region is enrichment. A provider that is down, rate-limiting or slow must cost
            // the visit its region, never the visit itself.
            log.warn("Region lookup failed for ip: {}", ip, e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IpApiResponse(String status, String regionName) {}
}
