package com.charishma.ratelimiter.core;

import com.charishma.ratelimiter.api.RateLimit.Algorithm;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DistributedRateLimiterTest {
    static Ignite ignite;
    DistributedRateLimiter limiter;

    @BeforeAll static void start() {
        ignite = Ignition.start(new IgniteConfiguration().setIgniteInstanceName("test-ignite"));
    }
    @AfterAll static void stop() { ignite.close(); }
    @BeforeEach void setup() { limiter = new DistributedRateLimiter(ignite); }

    @Test void tokenBucketStopsAfterLimit() {
        for (int i=0;i<5;i++) assertTrue(limiter.check("u1",5,60,Algorithm.TOKEN_BUCKET).allowed());
        assertFalse(limiter.check("u1",5,60,Algorithm.TOKEN_BUCKET).allowed());
    }

    @Test void slidingWindowStopsAfterLimit() {
        for (int i=0;i<3;i++) assertTrue(limiter.check("u2",3,60,Algorithm.SLIDING_WINDOW).allowed());
        assertFalse(limiter.check("u2",3,60,Algorithm.SLIDING_WINDOW).allowed());
    }
}