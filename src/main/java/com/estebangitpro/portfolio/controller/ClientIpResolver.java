package com.estebangitpro.portfolio.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Recovers the visitor's address from a request that reached the app through two proxies:
 * Cloudflare, then Traefik.
 *
 * <p>Order matters, and it is not arbitrary:
 *
 * <ol>
 *   <li>{@code CF-Connecting-IP} — set by Cloudflare and always the true client address.
 *   <li>{@code X-Forwarded-For} — first entry, for requests that did not come through Cloudflare.
 *   <li>{@code getRemoteAddr()} — the direct peer, which behind any proxy is the proxy itself.
 * </ol>
 *
 * <p>Cloudflare's header is preferred over {@code X-Forwarded-For} because Traefik does not
 * trust inbound forwarded headers unless {@code forwardedHeaders.trustedIPs} names the proxy
 * in front of it. Unconfigured — which is the Dokploy default — Traefik overwrites the header
 * with the peer it sees, so {@code X-Forwarded-For} carries a Cloudflare edge address and the
 * visitor's own address is lost.
 *
 * <p><strong>Neither header is proof of anything.</strong> A client that reaches the origin
 * directly, bypassing Cloudflare, can send whatever it likes in both. This value only ever
 * feeds analytics — it is hashed before storage and never authorises anything — so a forged
 * header costs a wrong row, nothing more. Do not reuse this for an access decision.
 */
@Component
public class ClientIpResolver {

    static final String CLOUDFLARE_HEADER = "CF-Connecting-IP";
    static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    public String resolve(HttpServletRequest request) {
        String cloudflareIp = blankToNull(request.getHeader(CLOUDFLARE_HEADER));
        if (cloudflareIp != null) {
            return cloudflareIp;
        }

        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null) {
            // The chain reads client, proxy1, proxy2, ...; only the first entry is the client.
            String client = blankToNull(forwardedFor.split(",")[0]);
            if (client != null) {
                return client;
            }
        }

        return request.getRemoteAddr();
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
