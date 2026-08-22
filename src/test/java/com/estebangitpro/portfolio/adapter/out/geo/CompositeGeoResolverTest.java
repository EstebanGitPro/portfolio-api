package com.estebangitpro.portfolio.adapter.out.geo;

import com.estebangitpro.portfolio.core.application.port.out.GeoResolver.GeoInfo;
import com.estebangitpro.portfolio.core.application.port.out.RegionResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifies the one thing the composite exists to do: carry the region across without
 * letting the second provider affect any other field — or the outcome.
 */
class CompositeGeoResolverTest {

    private static final String PUBLIC_IP = "200.24.16.1";

    /** Records what it was asked, so the "never called" cases can be asserted. */
    private static final class RecordingRegionResolver implements RegionResolver {
        private final String answer;
        private final RuntimeException failure;
        private int calls;

        private RecordingRegionResolver(String answer, RuntimeException failure) {
            this.answer = answer;
            this.failure = failure;
        }

        static RecordingRegionResolver answering(String answer) {
            return new RecordingRegionResolver(answer, null);
        }

        static RecordingRegionResolver throwing() {
            return new RecordingRegionResolver(null, new IllegalStateException("provider down"));
        }

        @Override
        public String resolve(String ip) {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return answer;
        }
    }

    private IpGuideGeoResolver primaryAnswering(String body) {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("https://ip.guide/" + PUBLIC_IP))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        return new IpGuideGeoResolver(restTemplate);
    }

    private static final String IP_GUIDE_BODY = """
            {"ip":"200.24.16.1","location":{"city":"Medellin","country":"Colombia",
             "timezone":"America/Bogota","latitude":6.2529,"longitude":-75.5646}}
            """;

    @Test
    void fills_in_the_region_and_leaves_every_other_field_untouched() {
        RecordingRegionResolver regionResolver = RecordingRegionResolver.answering("Antioquia");
        CompositeGeoResolver resolver =
                new CompositeGeoResolver(primaryAnswering(IP_GUIDE_BODY), regionResolver);

        GeoInfo info = resolver.resolve(PUBLIC_IP);

        assertThat(info.region()).isEqualTo("Antioquia");
        assertThat(info.country()).isEqualTo("Colombia");
        assertThat(info.city()).isEqualTo("Medellin");
        assertThat(info.timezone()).isEqualTo("America/Bogota");
        assertThat(info.latitude()).isEqualTo(6.2529);
        assertThat(info.longitude()).isEqualTo(-75.5646);
    }

    @Test
    void keeps_the_primary_result_when_the_region_lookup_returns_nothing() {
        CompositeGeoResolver resolver = new CompositeGeoResolver(
                primaryAnswering(IP_GUIDE_BODY), RecordingRegionResolver.answering(null));

        GeoInfo info = resolver.resolve(PUBLIC_IP);

        assertThat(info.region()).isNull();
        assertThat(info.country()).isEqualTo("Colombia");
        assertThat(info.city()).isEqualTo("Medellin");
    }

    @Test
    void skips_the_region_lookup_entirely_for_an_unroutable_address() {
        RecordingRegionResolver regionResolver = RecordingRegionResolver.answering("Antioquia");
        CompositeGeoResolver resolver =
                new CompositeGeoResolver(new IpGuideGeoResolver(new RestTemplate()), regionResolver);

        GeoInfo info = resolver.resolve("192.168.1.10");

        assertThat(info.country()).isEqualTo("Local");
        assertThat(info.region()).isNull();
        assertThat(regionResolver.calls).isZero();
    }

    @Test
    void a_throwing_region_resolver_costs_the_region_and_nothing_else() {
        CompositeGeoResolver resolver = new CompositeGeoResolver(
                primaryAnswering(IP_GUIDE_BODY), RecordingRegionResolver.throwing());

        GeoInfo info = resolver.resolve(PUBLIC_IP);

        assertThat(info.region()).isNull();
        assertThat(info.country()).isEqualTo("Colombia");
        assertThat(info.city()).isEqualTo("Medellin");
    }
}
