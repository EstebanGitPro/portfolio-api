package com.estebangitpro.portfolio.adapter.out.geo;

import com.estebangitpro.portfolio.core.application.port.out.GeoResolver;
import com.estebangitpro.portfolio.core.application.port.out.RegionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * The {@link GeoResolver} the application actually gets: {@link IpGuideGeoResolver} over HTTPS
 * for country, city, coordinates and timezone, plus a second lookup for the region that
 * provider does not return.
 *
 * <p>Marked {@link Primary} because {@link IpGuideGeoResolver} is itself a {@code GeoResolver}
 * bean; the composite is the one the core is wired to, and the delegate is an implementation
 * detail of this package.
 *
 * <p>Cost of the second field: one extra outbound request per tracked view, inside the request
 * thread. Both calls are bounded by the timeouts on the shared {@code RestTemplate}.
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class CompositeGeoResolver implements GeoResolver {

    private final IpGuideGeoResolver geoResolver;
    private final RegionResolver regionResolver;

    @Override
    public GeoInfo resolve(String ip) {
        GeoInfo geoInfo = geoResolver.resolve(ip);

        // A private address is already fully classified by the primary provider, and the
        // region provider would only answer "private range" — skip the round trip.
        if (PrivateIpRanges.isPrivate(ip)) {
            return geoInfo;
        }

        return new GeoInfo(
                geoInfo.country(),
                resolveRegionQuietly(ip),
                geoInfo.city(),
                geoInfo.latitude(),
                geoInfo.longitude(),
                geoInfo.timezone()
        );
    }

    /**
     * {@link RegionResolver} implementations are contracted not to throw, and the shipped one
     * honours it. This guard is the second line: adding a field must never be able to cost a
     * visit, so a misbehaving implementation degrades to a null region instead of failing the
     * tracked view.
     */
    private String resolveRegionQuietly(String ip) {
        try {
            return regionResolver.resolve(ip);
        } catch (Exception e) {
            log.warn("Region enrichment failed for ip: {}", ip, e);
            return null;
        }
    }
}
