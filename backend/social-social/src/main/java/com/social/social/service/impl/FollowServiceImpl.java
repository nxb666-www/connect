package com.social.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.social.entity.Follow;
import com.social.social.entity.Friend;
import com.social.social.mapper.FollowMapper;
import com.social.social.mapper.FriendMapper;
import com.social.social.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final FriendMapper friendMapper;

    @Override
    @Transactional
    public void follow(Long userId, Long followId) {
        if (userId.equals(followId)) {
            throw new BusinessException("不能关注自己");
        }

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, followId);
        if (followMapper.selectCount(wrapper) > 0) {
            return;
        }

        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowId(followId);
        followMapper.insert(follow);

        // 检查对方是否也关注了自己（互关）
        LambdaQueryWrapper<Follow> reverseWrapper = new LambdaQueryWrapper<>();
        reverseWrapper.eq(Follow::getUserId, followId)
                .eq(Follow::getFollowId, userId);
        if (followMapper.selectCount(reverseWrapper) > 0) {
            // 互关成功，自动创建双向好友关系
            createFriendIfNotExists(userId, followId);
        }
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long followId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, followId);
        followMapper.delete(wrapper);

        // 取消关注时删除好友关系
        removeFriend(userId, followId);
    }

    private void createFriendIfNotExists(Long userId, Long friendId) {
        // 检查是否已经是好友
        LambdaQueryWrapper<Friend> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.and(w -> w
                .and(w2 -> w2.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                .or(w2 -> w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId))
        );
        if (friendMapper.selectCount(checkWrapper) > 0) {
            return;
        }
        // 创建双向好友关系
        Friend f1 = new Friend();
        f1.setUserId(userId);
        f1.setFriendId(friendId);
        f1.setStatus(1);
        friendMapper.insert(f1);

        Friend f2 = new Friend();
        f2.setUserId(friendId);
        f2.setFriendId(userId);
        f2.setStatus(1);
        friendMapper.insert(f2);
    }

    private void removeFriend(Long userId, Long friendId) {
        // 删除双向好友关系
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w2 -> w2.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                .or(w2 -> w2.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId))
        );
        friendMapper.delete(wrapper);
    }

    @Override
    public List<Long> getFollowingList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        return followMapper.selectList(wrapper).stream()
                .map(Follow::getFollowId)
                .toList();
    }

    @Override
    public List<Long> getFollowerList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowId, userId);
        return followMapper.selectList(wrapper).stream()
                .map(Follow::getUserId)
                .toList();
    }

    @Override
    public Long getFollowingCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        return followMapper.selectCount(wrapper);
    }

    @Override
    public Long getFollowerCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowId, userId);
        return followMapper.selectCount(wrapper);
    }

    @Override
    public Boolean isFollowing(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowId, targetUserId);
        return followMapper.selectCount(wrapper) > 0;
    }
}
