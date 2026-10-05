package com.charishma.ratelimiter.web;

import com.charishma.ratelimiter.api.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {
    @RateLimit(key="#request.remoteAddress", limit=10, windowSeconds=60)
    @GetMapping("/demo")
    public String demo(HttpServletRequest request) {
        return "Request accepted";
    }
}