package com.estebangitpro.portfolio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests over the header precedence. Each case is a shape a real request can
 * actually arrive in, given Cloudflare in front of Traefik in front of the app.
 */
class ClientIpResolverTest {

    private static final String VISITOR = "200.24.16.1";
    private static final String CLOUDFLARE_EDGE = "172.71.18.5";
    private static final String TRAEFIK_PEER = "10.0.1.7";

    private final ClientIpResolver resolver = new ClientIpResolver();

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(TRAEFIK_PEER);
        return request;
    }

    @Test
    void prefers_the_cloudflare_header_over_everything_else() {
        MockHttpServletRequest request = request();
        request.addHeader(ClientIpResolver.CLOUDFLARE_HEADER, VISITOR);
        // What Traefik actually writes today: the edge it sees, not the visitor.
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, CLOUDFLARE_EDGE);

        assertThat(resolver.resolve(request)).isEqualTo(VISITOR);
    }

    @Test
    void falls_back_to_the_first_forwarded_for_entry() {
        MockHttpServletRequest request = request();
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, VISITOR + ", " + CLOUDFLARE_EDGE);

        assertThat(resolver.resolve(request)).isEqualTo(VISITOR);
    }

    @Test
    void falls_back_to_the_peer_address_when_no_header_is_present() {
        assertThat(resolver.resolve(request())).isEqualTo(TRAEFIK_PEER);
    }

    @Test
    void ignores_a_blank_cloudflare_header() {
        MockHttpServletRequest request = request();
        request.addHeader(ClientIpResolver.CLOUDFLARE_HEADER, "   ");
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, VISITOR);

        assertThat(resolver.resolve(request)).isEqualTo(VISITOR);
    }

    @Test
    void ignores_a_forwarded_for_whose_first_entry_is_empty() {
        MockHttpServletRequest request = request();
        request.addHeader(ClientIpResolver.FORWARDED_FOR_HEADER, ", " + CLOUDFLARE_EDGE);

        assertThat(resolver.resolve(request)).isEqualTo(TRAEFIK_PEER);
    }

    @Test
    void handles_an_ipv6_visitor() {
        MockHttpServletRequest request = request();
        request.addHeader(ClientIpResolver.CLOUDFLARE_HEADER, "2a02:c207:2309:5587::1");

        assertThat(resolver.resolve(request)).isEqualTo("2a02:c207:2309:5587::1");
    }
}
