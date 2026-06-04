package com.social.social.service;

import java.util.List;

public interface FollowService {

    void follow(Long userId, Long followId);

    void unfollow(Long userId, Long followId);

    List<Long> getFollowingList(Long userId);

    List<Long> getFollowerList(Long userId);

    Long getFollowingCount(Long userId);

    Long getFollowerCount(Long userId);

    Boolean isFollowing(Long userId, Long targetUserId);
}
