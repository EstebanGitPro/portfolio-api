package com.estebangitpro.portfolio.adapter.out.parser;

import com.estebangitpro.portfolio.core.application.port.out.DeviceParser;
import org.springframework.stereotype.Component;

@Component
public class UserAgentDeviceParser implements DeviceParser {

    private static final String DESKTOP = "DESKTOP";
    private static final String MOBILE = "MOBILE";
    private static final String TABLET = "TABLET";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String WINDOWS = "WINDOWS";
    private static final String MACOS = "MACOS";
    private static final String LINUX = "LINUX";
    private static final String ANDROID = "ANDROID";
    private static final String IOS = "IOS";
    private static final String CHROME = "CHROME";
    private static final String SAFARI = "SAFARI";
    private static final String FIREFOX = "FIREFOX";
    private static final String EDGE = "EDGE";
    private static final String OTHER = "OTHER";

    @Override
    public DeviceInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new DeviceInfo(DESKTOP, UNKNOWN, OTHER);
        }
        return new DeviceInfo(detectDeviceType(userAgent), detectOs(userAgent), detectBrowser(userAgent));
    }

    private String detectDeviceType(String ua) {
        if (ua.contains("iPad") || (ua.contains("Android") && ua.contains("Tablet")) || ua.contains("Kindle")) {
            return TABLET;
        }
        if (ua.contains("Android") || ua.contains("iPhone") || ua.contains("iPod")
                || ua.contains("Mobile") || ua.contains("Windows Phone")) {
            return MOBILE;
        }
        return DESKTOP;
    }

    private String detectOs(String ua) {
        if (ua.contains("Windows")) return WINDOWS;
        if (ua.contains("Mac OS X")) return MACOS;
        if (ua.contains("Android")) return ANDROID;
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) return IOS;
        if (ua.contains("Linux")) return LINUX;
        return UNKNOWN;
    }

    private String detectBrowser(String ua) {
        if (ua.contains("Edg/")) return EDGE;
        if (ua.contains("OPR/") || ua.contains("Opera")) return OTHER;
        if (ua.contains("Chrome/")) return CHROME;
        if (ua.contains("Safari/")) return SAFARI;
        if (ua.contains("Firefox/")) return FIREFOX;
        return OTHER;
    }
}
