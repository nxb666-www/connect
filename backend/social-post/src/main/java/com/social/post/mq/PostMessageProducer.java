package com.social.post.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendPostCreatedMessage(Long postId, Long userId, String contentPreview) {
        try {
            String payload = postId + ":" + userId + ":" + (contentPreview != null ? contentPreview : "");
            rocketMQTemplate.send("post-topic",
                    MessageBuilder.withPayload(payload).build());
            log.info("RocketMQ sent post-topic: postId={}, userId={}", postId, userId);
        } catch (Exception e) {
            log.warn("RocketMQ not available, message skipped: {}", e.getMessage());
        }
    }
}
