package com.social.social.service;

import java.util.List;

public interface BlacklistService {

    void addToBlacklist(Long userId, Long blackId);

    void removeFromBlacklist(Long userId, Long blackId);

    List<Long> getBlacklist(Long userId);

    Boolean isBlocked(Long userId, Long targetUserId);
}
