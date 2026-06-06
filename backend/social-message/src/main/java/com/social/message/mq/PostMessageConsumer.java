package com.social.message.mq;

import com.social.message.entity.Notification;
import com.social.message.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.name-server")
@RocketMQMessageListener(topic = "post-topic", consumerGroup = "social-message-group")
public class PostMessageConsumer implements RocketMQListener<String> {

    private final NotificationMapper notificationMapper;

    @Override
    public void onMessage(String message) {
        log.info("RocketMQ received post-topic: {}", message);
        try {
            String[] parts = message.split(":", 3);
            Long postId = Long.parseLong(parts[0]);
            Long userId = Long.parseLong(parts[1]);
            String contentPreview = parts.length > 2 ? parts[2] : "";

            // 写入系统通知
            Notification notification = new Notification();
            notification.setSenderId(userId);
            notification.setReceiverId(userId); // 自己的通知（可在后续扩展为粉丝通知）
            notification.setType("system");
            notification.setContent("你发布了新动态：" + contentPreview);
            notification.setTargetId(postId);
            notification.setIsRead(0);
            notificationMapper.insert(notification);

            log.info("Notification created for post: postId={}, userId={}", postId, userId);
        } catch (Exception e) {
            log.error("Message processing failed: {}", e.getMessage(), e);
        }
    }
}
