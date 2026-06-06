package com.social.message.service;

import com.social.message.entity.ChatGroup;
import com.social.message.entity.GroupMember;

import java.util.List;
import java.util.Map;

public interface GroupService {

    Long createGroup(Long ownerId, String name, List<Long> memberIds);

    ChatGroup getGroupInfo(Long groupId);

    List<Map<String, Object>> getMyGroups(Long userId);

    void addMember(Long groupId, Long operatorId, Long userId);

    void removeMember(Long groupId, Long operatorId, Long userId);

    List<Map<String, Object>> getGroupMembers(Long groupId);

    void deleteGroup(Long groupId, Long ownerId);

    void quitGroup(Long groupId, Long userId);

    boolean isMember(Long groupId, Long userId);
}
