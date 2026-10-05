package com.charishma.ratelimiter.api;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key();
    long limit() default 10;
    long windowSeconds() default 60;
    Algorithm algorithm() default Algorithm.TOKEN_BUCKET;
    boolean failOpen() default true;

    enum Algorithm { TOKEN_BUCKET, SLIDING_WINDOW }
}