package com.estebangitpro.portfolio.external;

public interface DeviceParser {
    DeviceInfo parse(String userAgent);

    record DeviceInfo(String deviceType, String operatingSystem, String browser) {}
}
