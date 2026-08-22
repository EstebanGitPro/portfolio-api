package com.estebangitpro.portfolio.core.application.port.out;

/**
 * Resolves the sub-national region (state, province, department) for an IP address.
 *
 * <p>Separate from {@link GeoResolver} because no single free provider returns every field:
 * the primary provider supplies country, city and coordinates but has no region at all.
 * Region is enrichment, never a reason to fail — implementations return {@code null} when
 * the answer is unknown and must not throw.
 */
public interface RegionResolver {

    String resolve(String ip);
}
