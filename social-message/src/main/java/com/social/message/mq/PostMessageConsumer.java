package com.social.message.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(topic = "post-topic", consumerGroup = "social-message-group")
public class PostMessageConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("RocketMQ received: {}", message);
        try {
            String[] parts = message.split(":");
            Long postId = Long.parseLong(parts[0]);
            Long userId = Long.parseLong(parts[1]);
            log.info("Processing post event: postId={}, userId={}", postId, userId);
        } catch (Exception e) {
            log.error("Message processing failed: {}", e.getMessage());
        }
    }
}
