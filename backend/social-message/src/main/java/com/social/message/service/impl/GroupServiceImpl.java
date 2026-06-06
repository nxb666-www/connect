package com.social.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.message.entity.ChatGroup;
import com.social.message.entity.GroupMember;
import com.social.message.feign.UserFeignClient;
import com.social.message.mapper.ChatGroupMapper;
import com.social.message.mapper.GroupMemberMapper;
import com.social.message.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final ChatGroupMapper chatGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public Long createGroup(Long ownerId, String name, List<Long> memberIds) {
        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setOwnerId(ownerId);
        group.setMemberCount(1 + (memberIds != null ? memberIds.size() : 0));
        chatGroupMapper.insert(group);

        // 添加群主
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(ownerId);
        owner.setRole(2); // 群主
        groupMemberMapper.insert(owner);

        // 添加成员
        if (memberIds != null) {
            for (Long memberId : memberIds) {
                if (!memberId.equals(ownerId)) {
                    GroupMember member = new GroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(memberId);
                    member.setRole(0);
                    groupMemberMapper.insert(member);
                }
            }
        }

        return group.getId();
    }

    @Override
    public ChatGroup getGroupInfo(Long groupId) {
        return chatGroupMapper.selectById(groupId);
    }

    @Override
    public List<Map<String, Object>> getMyGroups(Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getUserId, userId);
        List<GroupMember> memberships = groupMemberMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMember m : memberships) {
            ChatGroup group = chatGroupMapper.selectById(m.getGroupId());
            if (group != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", group.getId());
                info.put("name", group.getName());
                info.put("avatar", group.getAvatar());
                info.put("memberCount", group.getMemberCount());
                info.put("role", m.getRole());
                result.add(info);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void addMember(Long groupId, Long operatorId, Long userId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) throw new BusinessException("群聊不存在");

        // 检查操作者是否是群成员
        if (!isMember(groupId, operatorId)) throw new BusinessException("你不是群成员");

        // 检查目标用户是否已在群中
        if (isMember(groupId, userId)) throw new BusinessException("用户已在群中");

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(0);
        groupMemberMapper.insert(member);

        group.setMemberCount(group.getMemberCount() + 1);
        chatGroupMapper.updateById(group);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long operatorId, Long userId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) throw new BusinessException("群聊不存在");

        // 只有群主和管理员可以踢人
        GroupMember operator = getMemberRecord(groupId, operatorId);
        if (operator == null || operator.getRole() < 1) throw new BusinessException("权限不足");

        // 不能踢群主
        if (userId.equals(group.getOwnerId())) throw new BusinessException("不能移除群主");

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);

        group.setMemberCount(Math.max(group.getMemberCount() - 1, 0));
        chatGroupMapper.updateById(group);
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(wrapper);

        List<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toList());
        Map<Long, String[]> userInfoMap = new HashMap<>();
        for (Long uid : userIds) {
            try {
                var res = userFeignClient.getUserInfo(uid);
                if (res != null && res.getData() != null) {
                    Map<String, Object> u = res.getData();
                    userInfoMap.put(uid, new String[]{
                            (String) u.get("nickname"),
                            (String) u.get("avatar")
                    });
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}", uid);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMember m : members) {
            Map<String, Object> info = new HashMap<>();
            info.put("userId", m.getUserId());
            info.put("role", m.getRole());
            info.put("nickname", m.getNickname());
            String[] userInfo = userInfoMap.get(m.getUserId());
            if (userInfo != null) {
                info.put("userName", userInfo[0]);
                info.put("avatar", userInfo[1]);
            }
            result.add(info);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId, Long ownerId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) throw new BusinessException("群聊不存在");
        if (!group.getOwnerId().equals(ownerId)) throw new BusinessException("只有群主可以解散群聊");

        // 删除所有成员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(wrapper);

        chatGroupMapper.deleteById(groupId);
    }

    @Override
    @Transactional
    public void quitGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) throw new BusinessException("群聊不存在");

        if (group.getOwnerId().equals(userId)) throw new BusinessException("群主不能退群，请先转让或解散群聊");

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);

        group.setMemberCount(Math.max(group.getMemberCount() - 1, 0));
        chatGroupMapper.updateById(group);
    }

    @Override
    public boolean isMember(Long groupId, Long userId) {
        return getMemberRecord(groupId, userId) != null;
    }

    private GroupMember getMemberRecord(Long groupId, Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(wrapper);
    }
}
