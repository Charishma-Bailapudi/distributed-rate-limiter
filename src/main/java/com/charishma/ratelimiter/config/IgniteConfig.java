package com.charishma.ratelimiter.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteConfig {
    @Bean(destroyMethod = "close")
    public Ignite ignite() {
        IgniteConfiguration config = new IgniteConfiguration();
        config.setIgniteInstanceName("distributed-rate-limiter");
        return Ignition.start(config);
    }
}