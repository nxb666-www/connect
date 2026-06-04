package com.social.statistics.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "social-user")
public interface UserFeignClient {

    @GetMapping("/api/user/internal/stats")
    Map<String, Object> getUserStats(@RequestParam("date") String date);
}
