package com.charishma.ratelimiter.web;

import com.charishma.ratelimiter.api.RateLimit;
import com.charishma.ratelimiter.core.DistributedRateLimiter;
import com.charishma.ratelimiter.core.RateLimitResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

@Component
@Aspect
public class RateLimitAspect {
    private final DistributedRateLimiter limiter;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public RateLimitAspect(DistributedRateLimiter limiter) { this.limiter = limiter; }

    @Around("@annotation(rateLimit)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        try {
            String key = resolveKey(rateLimit.key());
            RateLimitResult result = limiter.check(key, rateLimit.limit(), rateLimit.windowSeconds(), rateLimit.algorithm());
            if (!result.allowed()) {
                HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
                if (response != null) {
                    response.setStatus(429);
                    response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
                }
                return "Rate limit exceeded";
            }
            return pjp.proceed();
        } catch (RuntimeException ex) {
            if (rateLimit.failOpen()) return pjp.proceed();
            throw ex;
        }
    }

    private String resolveKey(String expression) {
        var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        if ("#request.remoteAddress".equals(expression)) return request.getRemoteAddr();
        return expression;
    }
}