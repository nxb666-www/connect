package com.social.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.social.entity.Friend;
import com.social.social.mapper.FriendMapper;
import com.social.social.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;

    @Override
    @Transactional
    public void sendFriendRequest(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException("不能添加自己为好友");
        }

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        if (friendMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经发送过好友申请");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(0);
        friendMapper.insert(friend);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, 0);
        Friend request = friendMapper.selectOne(wrapper);
        if (request == null) {
            throw new BusinessException("好友申请不存在");
        }

        request.setStatus(1);
        friendMapper.updateById(request);

        Friend reverse = new Friend();
        reverse.setUserId(userId);
        reverse.setFriendId(friendId);
        reverse.setStatus(1);
        friendMapper.insert(reverse);
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, 0);
        friendMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                .or(w -> w.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId));
        friendMapper.delete(wrapper);
    }

    @Override
    public List<Friend> getFriendList(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getStatus, 1);
        return friendMapper.selectList(wrapper);
    }

    @Override
    public List<Friend> getPendingRequests(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, 0);
        return friendMapper.selectList(wrapper);
    }
}
