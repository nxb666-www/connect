package com.social.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.social.entity.Blacklist;
import com.social.social.mapper.BlacklistMapper;
import com.social.social.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistMapper blacklistMapper;

    @Override
    @Transactional
    public void addToBlacklist(Long userId, Long blackId) {
        if (userId.equals(blackId)) {
            throw new BusinessException("不能拉黑自己");
        }

        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlackId, blackId);
        if (blacklistMapper.selectCount(wrapper) > 0) {
            return;
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setBlackId(blackId);
        blacklistMapper.insert(blacklist);
    }

    @Override
    @Transactional
    public void removeFromBlacklist(Long userId, Long blackId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlackId, blackId);
        blacklistMapper.delete(wrapper);
    }

    @Override
    public List<Long> getBlacklist(Long userId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId);
        return blacklistMapper.selectList(wrapper).stream()
                .map(Blacklist::getBlackId)
                .toList();
    }

    @Override
    public Boolean isBlocked(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getUserId, userId)
                .eq(Blacklist::getBlackId, targetUserId);
        return blacklistMapper.selectCount(wrapper) > 0;
    }
}
