package com.social.message.feign;

import com.social.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "social-social")
public interface SocialFeignClient {

    @GetMapping("/api/social/internal/friend/check/{userId}/{targetUserId}")
    Result<Boolean> isFriend(@PathVariable("userId") Long userId, @PathVariable("targetUserId") Long targetUserId);

    @GetMapping("/api/social/internal/blacklist/check/{userId}/{targetUserId}")
    Result<Boolean> isBlocked(@PathVariable("userId") Long userId, @PathVariable("targetUserId") Long targetUserId);
}
