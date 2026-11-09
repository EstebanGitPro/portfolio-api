package com.estebangitpro.portfolio.external;

public interface GeoResolver {
    GeoInfo resolve(String ip);

    record GeoInfo(String country, String region, String city,
                   Double latitude, Double longitude, String timezone) {}
}
