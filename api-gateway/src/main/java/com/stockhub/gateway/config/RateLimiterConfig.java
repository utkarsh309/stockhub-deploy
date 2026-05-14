package com.stockhub.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    // KeyResolver — decides HOW to identify each client
    // We use IP address so each IP gets its own token bucket in Redis
    // X-Forwarded-For handles clients behind a proxy/load balancer
    // Falls back to remoteAddress if header is missing
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // First try X-Forwarded-For header (client behind proxy)
            String ip = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            // Fallback to remoteAddress safely — null check added
            if (ip == null || ip.isEmpty()) {
                InetSocketAddress remoteAddress = exchange
                        .getRequest()
                        .getRemoteAddress();

                ip = (remoteAddress != null)
                        ? remoteAddress.getAddress().getHostAddress()
                        : "unknown";   // fallback key if both are null
            }

            return Mono.just(ip);
        };
    }

    // RedisRateLimiter — token bucket algorithm
    // replenishRate = 10  → add 10 tokens per second to the bucket
    // burstCapacity  = 20 → bucket can hold max 20 tokens at once
    //                       allows short bursts (e.g. page load hits multiple endpoints)
    // requestedTokens = 1 → each request costs 1 token
    // Result: steady state = 10 req/sec per IP
    //  burst allowed up to 20 req/sec briefly
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
}