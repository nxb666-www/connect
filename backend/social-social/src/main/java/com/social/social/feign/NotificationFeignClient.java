package com.social.social.feign;

import com.social.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "social-message")
public interface NotificationFeignClient {

    @PostMapping("/api/notification/internal/send")
    Result<Void> sendNotification(@RequestBody Map<String, Object> dto);
}
