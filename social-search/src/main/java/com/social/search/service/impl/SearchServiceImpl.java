package com.social.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.search.entity.SearchHistory;
import com.social.search.feign.PostFeignClient;
import com.social.search.feign.UserFeignClient;
import com.social.search.mapper.SearchHistoryMapper;
import com.social.search.service.SearchService;
import com.social.search.vo.SearchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchHistoryMapper searchHistoryMapper;
    private final UserFeignClient userFeignClient;
    private final PostFeignClient postFeignClient;

    @Override
    @Transactional
    public SearchResultVO search(String keyword, Long userId) {
        if (userId != null) {
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setType("general");
            searchHistoryMapper.insert(history);
        }

        SearchResultVO result = new SearchResultVO();

        // search users
        try {
            var userResult = userFeignClient.searchUsers(keyword);
            if (userResult != null && userResult.getData() != null) {
                List<SearchResultVO.UserResult> users = new ArrayList<>();
                for (Map<String, Object> u : userResult.getData()) {
                    SearchResultVO.UserResult ur = new SearchResultVO.UserResult();
                    ur.setUserId(convertToLong(u.get("userId")));
                    ur.setUsername((String) u.get("username"));
                    ur.setNickname((String) u.get("nickname"));
                    ur.setAvatar((String) u.get("avatar"));
                    ur.setSignature((String) u.get("signature"));
                    users.add(ur);
                }
                result.setUsers(users);
            }
        } catch (Exception e) {
            log.warn("搜索用户失败: {}", e.getMessage());
            result.setUsers(new ArrayList<>());
        }

        // search posts and topics
        try {
            var postResult = postFeignClient.searchPosts(keyword);
            if (postResult != null && postResult.getData() != null) {
                Map<String, Object> data = postResult.getData();

                List<Map<String, Object>> posts = (List<Map<String, Object>>) data.getOrDefault("posts", new ArrayList<>());
                List<SearchResultVO.PostResult> postResults = new ArrayList<>();
                for (Map<String, Object> p : posts) {
                    SearchResultVO.PostResult pr = new SearchResultVO.PostResult();
                    pr.setPostId(convertToLong(p.get("id")));
                    pr.setContent((String) p.get("content"));
                    pr.setTopic((String) p.get("topic"));
                    pr.setLikeCount(p.get("likeCount") != null ? ((Number) p.get("likeCount")).intValue() : 0);
                    pr.setCommentCount(p.get("commentCount") != null ? ((Number) p.get("commentCount")).intValue() : 0);
                    pr.setNickname((String) p.get("nickname"));
                    pr.setUsername((String) p.get("username"));
                    postResults.add(pr);
                }
                result.setPosts(postResults);

                List<Map<String, Object>> topics = (List<Map<String, Object>>) data.getOrDefault("topics", new ArrayList<>());
                List<SearchResultVO.TopicResult> topicResults = new ArrayList<>();
                for (Map<String, Object> t : topics) {
                    SearchResultVO.TopicResult tr = new SearchResultVO.TopicResult();
                    tr.setTopic((String) t.get("topic"));
                    tr.setPostCount(t.get("postCount") != null ? ((Number) t.get("postCount")).longValue() : 0L);
                    topicResults.add(tr);
                }
                result.setTopics(topicResults);
            }
        } catch (Exception e) {
            log.warn("搜索动态失败: {}", e.getMessage());
            result.setPosts(new ArrayList<>());
            result.setTopics(new ArrayList<>());
        }

        return result;
    }

    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }

    @Override
    public List<SearchHistory> getSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime)
                .last("LIMIT 20");
        return searchHistoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void clearSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        searchHistoryMapper.delete(wrapper);
    }
}
