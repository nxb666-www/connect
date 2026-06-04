package com.social.statistics.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "social-post")
public interface PostFeignClient {

    @GetMapping("/api/post/internal/stats")
    Map<String, Object> getPostStats(@RequestParam("date") String date);
}
