package com.social.search.feign;

import com.social.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "social-user")
public interface UserFeignClient {

    @GetMapping("/api/user/internal/search")
    Result<List<Map<String, Object>>> searchUsers(@RequestParam("keyword") String keyword);
}
