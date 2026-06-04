package com.social.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.message.dto.MessageSendDTO;
import com.social.message.entity.Message;
import com.social.message.feign.UserFeignClient;
import com.social.message.mapper.MessageMapper;
import com.social.message.service.MessageService;
import com.social.message.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserFeignClient userFeignClient;

    private final Map<Long, String[]> userCache = new HashMap<>();

    private String[] getUserInfo(Long userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        try {
            Result<Map<String, Object>> result = userFeignClient.getUserInfo(userId);
            if (result != null && result.getData() != null) {
                Map<String, Object> data = result.getData();
                String name = (String) data.getOrDefault("nickname", data.getOrDefault("username", "用户"));
                String avatar = (String) data.getOrDefault("avatar", "");
                String[] info = new String[]{name, avatar};
                userCache.put(userId, info);
                return info;
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: userId={}", userId);
        }
        return new String[]{"用户", ""};
    }

    @Override
    @Transactional
    public Long sendMessage(Long senderId, MessageSendDTO dto) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setType(dto.getType());
        message.setIsRead(0);
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public Page<MessageVO> getConversation(Long userId, Long targetUserId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w2 -> w2.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetUserId))
                .or(w2 -> w2.eq(Message::getSenderId, targetUserId).eq(Message::getReceiverId, userId))
        ).orderByDesc(Message::getCreateTime);
        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);

        Page<MessageVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        return voPage;
    }

    @Override
    public List<MessageVO> getRecentConversations(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getSenderId, userId)
                .or()
                .eq(Message::getReceiverId, userId)
        ).orderByDesc(Message::getCreateTime);
        List<Message> messages = messageMapper.selectList(wrapper);

        Map<Long, Message> latestByPartner = new LinkedHashMap<>();
        for (Message m : messages) {
            Long partnerId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            latestByPartner.putIfAbsent(partnerId, m);
        }

        return latestByPartner.values().stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long senderId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getSenderId, senderId)
                .eq(Message::getIsRead, 0);
        List<Message> messages = messageMapper.selectList(wrapper);
        for (Message message : messages) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());

        String[] senderInfo = getUserInfo(message.getSenderId());
        vo.setSenderName(senderInfo[0]);
        vo.setSenderAvatar(senderInfo[1]);

        String[] receiverInfo = getUserInfo(message.getReceiverId());
        vo.setReceiverName(receiverInfo[0]);
        vo.setReceiverAvatar(receiverInfo[1]);

        return vo;
    }
}
