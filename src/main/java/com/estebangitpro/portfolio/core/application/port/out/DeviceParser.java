package com.estebangitpro.portfolio.core.application.port.out;

public interface DeviceParser {
    DeviceInfo parse(String userAgent);

    record DeviceInfo(String deviceType, String operatingSystem, String browser) {}
}
