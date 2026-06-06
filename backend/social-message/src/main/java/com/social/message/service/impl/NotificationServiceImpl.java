package com.social.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.message.dto.NotificationSendDTO;
import com.social.message.entity.Notification;
import com.social.message.feign.UserFeignClient;
import com.social.message.mapper.NotificationMapper;
import com.social.message.service.NotificationService;
import com.social.message.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public void sendNotification(NotificationSendDTO dto) {
        if (dto.getReceiverId().equals(dto.getSenderId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverId(dto.getReceiverId());
        notification.setSenderId(dto.getSenderId());
        notification.setType(dto.getType());
        notification.setContent(dto.getContent());
        notification.setTargetId(dto.getTargetId());
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public Page<NotificationVO> getNotifications(Long userId, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> notificationPage = notificationMapper.selectPage(page, wrapper);

        Page<NotificationVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(notificationPage.getTotal());
        voPage.setRecords(notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        return voPage;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
                .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getReceiverId, userId)
                .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setSenderId(notification.getSenderId());
        vo.setType(notification.getType());
        vo.setContent(notification.getContent());
        vo.setTargetId(notification.getTargetId());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());

        try {
            Result<Map<String, Object>> result = userFeignClient.getUserInfo(notification.getSenderId());
            if (result != null && result.getData() != null) {
                Map<String, Object> data = result.getData();
                vo.setSenderName((String) data.getOrDefault("nickname", data.getOrDefault("username", "用户")));
                vo.setSenderAvatar((String) data.getOrDefault("avatar", ""));
            } else {
                vo.setSenderName("用户");
                vo.setSenderAvatar("");
            }
        } catch (Exception e) {
            log.warn("获取发送者信息失败: senderId={}", notification.getSenderId());
            vo.setSenderName("用户");
            vo.setSenderAvatar("");
        }

        return vo;
    }
}
