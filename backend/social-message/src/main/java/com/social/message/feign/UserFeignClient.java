package com.social.message.feign;

import com.social.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "social-user")
public interface UserFeignClient {

    @GetMapping("/api/user/internal/{userId}")
    Result<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);
}
