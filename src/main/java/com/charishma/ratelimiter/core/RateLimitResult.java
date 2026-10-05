package com.charishma.ratelimiter.core;

public record RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {}