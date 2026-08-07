package com.demo.upimesh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    
    // Fallback for dev profile (when Redis is not available)
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();

    @Autowired
    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean claim(String packetHash) {
        try {
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(packetHash, "1", Duration.ofHours(24));
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            // Fallback to local cache if Redis unavailable
            return localCache.putIfAbsent(packetHash, "1") == null;
        }
    }

    public void release(String packetHash) {
        try {
            redisTemplate.delete(packetHash);
        } catch (Exception e) {
            localCache.remove(packetHash);
        }
    }

    // Methods needed by ApiController
    public int size() {
        try {
            // Redis doesn't have a simple size for all keys, so we use local cache size as proxy in dev
            // In production, this would use Redis SCAN or INFO keyspace
            return localCache.size();
        } catch (Exception e) {
            return localCache.size();
        }
    }

    public void clear() {
        try {
            // Note: In production with Redis, this would flush the specific prefix, not all keys
            localCache.clear();
        } catch (Exception e) {
            localCache.clear();
        }
    }
}
