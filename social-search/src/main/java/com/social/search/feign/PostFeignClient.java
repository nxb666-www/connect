package com.social.search.feign;

import com.social.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "social-post")
public interface PostFeignClient {

    @GetMapping("/api/post/internal/search")
    Result<Map<String, Object>> searchPosts(@RequestParam("keyword") String keyword);
}
