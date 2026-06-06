package com.social.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.common.core.result.Result;
import com.social.message.dto.MessageSendDTO;
import com.social.message.entity.Message;
import com.social.common.core.exception.BusinessException;
import com.social.message.entity.ChatGroup;
import com.social.message.feign.SocialFeignClient;
import com.social.message.feign.UserFeignClient;
import com.social.message.mapper.MessageMapper;
import com.social.message.service.GroupService;
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
    private final SocialFeignClient socialFeignClient;
    private final GroupService groupService;

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
        Long groupId = dto.getGroupId();

        // 群聊消息
        if (groupId != null) {
            if (!groupService.isMember(groupId, senderId)) {
                throw new BusinessException("你不是群成员，无法发送消息");
            }
            Message message = new Message();
            message.setSenderId(senderId);
            message.setGroupId(groupId);
            message.setChatType(2);
            message.setContent(dto.getContent());
            message.setType(dto.getType());
            message.setIsRead(0);
            messageMapper.insert(message);
            return message.getId();
        }

        // 私聊消息
        Long receiverId = dto.getReceiverId();

        try {
            Result<Boolean> friendResult = socialFeignClient.isFriend(senderId, receiverId);
            if (friendResult == null || !Boolean.TRUE.equals(friendResult.getData())) {
                throw new BusinessException("对方不是你的好友，无法发送消息");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("好友关系校验失败: {}", e.getMessage());
        }

        try {
            Result<Boolean> blockResult = socialFeignClient.isBlocked(senderId, receiverId);
            if (blockResult != null && Boolean.TRUE.equals(blockResult.getData())) {
                throw new BusinessException("你已将对方拉黑，无法发送消息");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("黑名单校验失败: {}", e.getMessage());
        }

        try {
            Result<Boolean> blockResult2 = socialFeignClient.isBlocked(receiverId, senderId);
            if (blockResult2 != null && Boolean.TRUE.equals(blockResult2.getData())) {
                throw new BusinessException("对方已将你拉黑，无法发送消息");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("黑名单校验失败: {}", e.getMessage());
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setChatType(1);
        message.setContent(dto.getContent());
        message.setType(dto.getType());
        message.setIsRead(0);
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public Page<MessageVO> getGroupConversation(Long userId, Long groupId, int pageNum, int pageSize) {
        if (!groupService.isMember(groupId, userId)) {
            throw new BusinessException("你不是群成员");
        }
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getGroupId, groupId)
                .eq(Message::getChatType, 2)
                .orderByDesc(Message::getCreateTime);
        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);

        Page<MessageVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        return voPage;
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
        // 私聊会话
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(Message::getSenderId, userId)
                .or()
                .eq(Message::getReceiverId, userId)
        ).eq(Message::getChatType, 1)
                .orderByDesc(Message::getCreateTime);
        List<Message> messages = messageMapper.selectList(wrapper);

        Map<Long, Message> latestByPartner = new LinkedHashMap<>();
        for (Message m : messages) {
            Long partnerId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            latestByPartner.putIfAbsent(partnerId, m);
        }

        List<MessageVO> result = latestByPartner.values().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 群聊会话
        List<Map<String, Object>> groups = groupService.getMyGroups(userId);
        for (Map<String, Object> group : groups) {
            Long groupId = (Long) group.get("id");
            LambdaQueryWrapper<Message> groupWrapper = new LambdaQueryWrapper<>();
            groupWrapper.eq(Message::getGroupId, groupId)
                    .eq(Message::getChatType, 2)
                    .orderByDesc(Message::getCreateTime)
                    .last("LIMIT 1");
            List<Message> groupMessages = messageMapper.selectList(groupWrapper);
            if (!groupMessages.isEmpty()) {
                MessageVO vo = convertToVO(groupMessages.get(0));
                vo.setReceiverId(groupId);
                vo.setReceiverName((String) group.get("name"));
                vo.setReceiverAvatar((String) group.get("avatar"));
                result.add(vo);
            }
        }

        // 按时间排序
        result.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        return result;
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

    @Override
    @Transactional
    public void markGroupAsRead(Long userId, Long groupId) {
        // 群聊消息标记已读：将用户加入群之前的最后一条消息之前的消息标记已读
        // 简化实现：将该群所有消息标记为已读
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getGroupId, groupId)
                .eq(Message::getChatType, 2)
                .ne(Message::getSenderId, userId)
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
        vo.setGroupId(message.getGroupId());
        vo.setChatType(message.getChatType());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());

        String[] senderInfo = getUserInfo(message.getSenderId());
        vo.setSenderName(senderInfo[0]);
        vo.setSenderAvatar(senderInfo[1]);

        // 群聊消息设置群名
        if (message.getChatType() != null && message.getChatType() == 2 && message.getGroupId() != null) {
            ChatGroup group = groupService.getGroupInfo(message.getGroupId());
            if (group != null) {
                vo.setReceiverId(message.getGroupId());
                vo.setReceiverName(group.getName());
                vo.setReceiverAvatar(group.getAvatar());
            }
        } else if (message.getReceiverId() != null) {
            String[] receiverInfo = getUserInfo(message.getReceiverId());
            vo.setReceiverName(receiverInfo[0]);
            vo.setReceiverAvatar(receiverInfo[1]);
        }

        return vo;
    }
}
