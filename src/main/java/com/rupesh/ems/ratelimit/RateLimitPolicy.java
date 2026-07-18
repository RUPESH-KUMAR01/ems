package com.rupesh.ems.ratelimit;

import java.time.Duration;

public record RateLimitPolicy(int capacity, int refillTokens, Duration refillDuration) {}
