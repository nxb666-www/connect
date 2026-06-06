package com.social.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.message.dto.NotificationSendDTO;
import com.social.message.vo.NotificationVO;

public interface NotificationService {

    void sendNotification(NotificationSendDTO dto);

    Page<NotificationVO> getNotifications(Long userId, int pageNum, int pageSize);

    Long getUnreadCount(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);
}
