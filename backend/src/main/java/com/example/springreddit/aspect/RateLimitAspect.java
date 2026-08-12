package com.example.springreddit.aspect;

import com.example.springreddit.annotation.RateLimit;
import com.example.springreddit.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final HttpServletRequest request;

    public RateLimitAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@annotation(rateLimitAnnotation)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimitAnnotation) throws Throwable {

        String ip = request.getRemoteAddr();

        String methodName = joinPoint.getSignature().getName();

        String key = ip + ":" + methodName;
        System.out.println(key);

        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket(rateLimitAnnotation));

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