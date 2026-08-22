package com.estebangitpro.portfolio.adapter.out.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boundary tests around the RFC 1918 slice of the 172 block: only 172.16.0.0/12 is
 * private, so a public 172.x address must still reach the geolocation providers.
 */
class PrivateIpRangesTest {

    @Test
    void the_private_slice_of_the_172_block_starts_at_16() {
        assertThat(PrivateIpRanges.isPrivate("172.15.255.255")).isFalse();
        assertThat(PrivateIpRanges.isPrivate("172.16.0.0")).isTrue();
    }

    @Test
    void the_private_slice_of_the_172_block_ends_at_31() {
        assertThat(PrivateIpRanges.isPrivate("172.31.255.255")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("172.32.0.0")).isFalse();
    }

    @Test
    void other_addresses_in_the_172_block_are_public() {
        assertThat(PrivateIpRanges.isPrivate("172.0.0.1")).isFalse();
        assertThat(PrivateIpRanges.isPrivate("172.99.1.1")).isFalse();
        assertThat(PrivateIpRanges.isPrivate("172.255.255.255")).isFalse();
    }

    @Test
    void every_other_private_range_stays_private() {
        assertThat(PrivateIpRanges.isPrivate(null)).isTrue();
        assertThat(PrivateIpRanges.isPrivate("  ")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("127.0.0.1")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("10.0.0.4")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("192.168.1.10")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("::1")).isTrue();
        assertThat(PrivateIpRanges.isPrivate("0:0:0:0:0:0:0:1")).isTrue();
    }

    @Test
    void malformed_addresses_are_never_reported_as_private_172() {
        assertThat(PrivateIpRanges.isPrivate("172.20")).isFalse();
        assertThat(PrivateIpRanges.isPrivate("172.notanumber.1.1")).isFalse();
    }
}
