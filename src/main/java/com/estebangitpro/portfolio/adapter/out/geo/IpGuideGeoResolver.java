package com.estebangitpro.portfolio.adapter.out.geo;

import com.estebangitpro.portfolio.core.application.port.out.GeoResolver;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class IpGuideGeoResolver implements GeoResolver {

    private static final String IP_GUIDE_URL = "https://ip.guide/%s";

    private final RestTemplate restTemplate;

    @Override
    public GeoInfo resolve(String ip) {
        try {
            if (isPrivateIp(ip)) {
                return new GeoInfo("Local", null, null, null, null, null);
            }

            String url = String.format(IP_GUIDE_URL, ip);
            IpGuideResponse response = restTemplate.getForObject(url, IpGuideResponse.class);

            if (response != null && response.location() != null) {
                Location loc = response.location();
                return new GeoInfo(
                        loc.country(), null, loc.city(),
                        loc.latitude(), loc.longitude(), loc.timezone()
                );
            }
            return new GeoInfo(null, null, null, null, null, null);
        } catch (Exception e) {
            log.warn("GeoIP lookup failed for ip: {}", ip, e);
            return new GeoInfo(null, null, null, null, null, null);
        }
    }

    private boolean isPrivateIp(String ip) {
        return ip == null
                || ip.startsWith("127.")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("172.")
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IpGuideResponse(String ip, Location location, AutonomousSystem autonomousSystem) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String city, String country, String timezone, Double latitude, Double longitude) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AutonomousSystem(Integer asn, String name, String organization, String country, String rir) {}
}
