package com.rupesh.ems.ratelimit;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.util.concurrent.ConcurrentHashMap;

public class RateLimitService {

  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  public Bucket resolveBucket(String key, RateLimitPolicy policy) {

    return buckets.computeIfAbsent(
        key,
        ignored ->
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(policy.capacity())
                        .refillGreedy(
                            policy.refillTokens(),
                            policy.refillDuration())
                        .build())
                .build());
  }
}