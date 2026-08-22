package com.estebangitpro.portfolio.adapter.out.geo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pure unit tests: the provider is stubbed, so every assertion is about how this adapter
 * behaves when the provider succeeds, misses or fails outright.
 */
class IpApiRegionResolverTest {

    private static final String PUBLIC_IP = "200.24.16.1";
    private static final String URL = "http://ip-api.com/json/" + PUBLIC_IP + "?fields=status,regionName";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private IpApiRegionResolver resolver;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        resolver = new IpApiRegionResolver(restTemplate);
    }

    @Test
    void returns_the_region_name_on_a_successful_lookup() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("""
                        {"status":"success","regionName":"Antioquia"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(resolver.resolve(PUBLIC_IP)).isEqualTo("Antioquia");
        server.verify();
    }

    @Test
    void returns_null_when_the_provider_reports_a_failed_lookup() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("""
                        {"status":"fail","message":"reserved range"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(resolver.resolve(PUBLIC_IP)).isNull();
    }

    @Test
    void returns_null_when_the_provider_answers_without_a_region() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("""
                        {"status":"success","regionName":""}
                        """, MediaType.APPLICATION_JSON));

        assertThat(resolver.resolve(PUBLIC_IP)).isNull();
    }

    @Test
    void swallows_provider_failures_instead_of_propagating_them() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(resolver.resolve(PUBLIC_IP)).isNull();
    }

    @Test
    void never_calls_the_provider_for_an_unroutable_address() {
        // No expectations registered: any outbound call fails the test.
        assertThat(resolver.resolve("192.168.1.10")).isNull();
        assertThat(resolver.resolve("127.0.0.1")).isNull();
        assertThat(resolver.resolve("10.0.0.4")).isNull();
        assertThat(resolver.resolve("::1")).isNull();
        assertThat(resolver.resolve(null)).isNull();

        server.verify();
    }
}
