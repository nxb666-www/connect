package com.social.social.service;

import com.social.social.entity.Friend;

import java.util.List;

public interface FriendService {

    void sendFriendRequest(Long userId, Long friendId);

    void acceptFriendRequest(Long userId, Long friendId);

    void rejectFriendRequest(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<Friend> getFriendList(Long userId);

    List<Friend> getPendingRequests(Long userId);

    Boolean isFriend(Long userId, Long targetUserId);
}
