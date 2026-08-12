package com.example.springreddit.aspect;

import com.example.springreddit.annotation.RateLimit;
import com.example.springreddit.exception.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class RateLimitAspect {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(2))
            .maximumSize(10_000)
            .build();

    private final HttpServletRequest request;

    public RateLimitAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@annotation(rateLimitAnnotation)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimitAnnotation) throws Throwable {

        String ip = request.getRemoteAddr();
        String methodName = joinPoint.getSignature().getName();
        String key = ip + ":" + methodName;

        Bucket bucket = cache.get(key, k -> createNewBucket(rateLimitAnnotation));

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }

        return joinPoint.proceed();
    }

    private Bucket createNewBucket(RateLimit annotation) {
        Duration timeWindow = Duration.of(annotation.duration(), annotation.unit().toChronoUnit());
        Refill refill = Refill.greedy(annotation.requests(), timeWindow);
        Bandwidth limit = Bandwidth.classic(annotation.requests(), refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}