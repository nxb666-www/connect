package com.social.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.message.dto.MessageSendDTO;
import com.social.message.vo.MessageVO;

import java.util.List;

public interface MessageService {

    Long sendMessage(Long senderId, MessageSendDTO dto);

    Page<MessageVO> getConversation(Long userId, Long targetUserId, int pageNum, int pageSize);

    Page<MessageVO> getGroupConversation(Long userId, Long groupId, int pageNum, int pageSize);

    List<MessageVO> getRecentConversations(Long userId);

    Long getUnreadCount(Long userId);

    void markAsRead(Long userId, Long senderId);

    void markGroupAsRead(Long userId, Long groupId);
}
