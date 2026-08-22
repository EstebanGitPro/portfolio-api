package com.estebangitpro.portfolio.adapter.out.geo;

/**
 * Recognises addresses that no public geolocation provider can resolve.
 *
 * <p>Shared by every geo adapter so a visitor is classified the same way no matter which
 * provider answers, and so an unroutable address never costs an outbound call.
 */
final class PrivateIpRanges {

    private PrivateIpRanges() {
    }

    static boolean isPrivate(String ip) {
        return ip == null
                || ip.isBlank()
                || ip.startsWith("127.")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || isRfc1918_172Block(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip);
    }

    /**
     * RFC 1918 reserves only 172.16.0.0/12 inside the 172 block, so the second
     * octet must be between 16 and 31; every other 172.x address is public.
     */
    private static boolean isRfc1918_172Block(String ip) {
        String[] octets = ip.split("\\.", -1);
        if (octets.length != 4 || !"172".equals(octets[0])) {
            return false;
        }
        int second;
        try {
            second = Integer.parseInt(octets[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        return second >= 16 && second <= 31;
    }
}
