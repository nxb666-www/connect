package com.social.post.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.social.post.dto.PostCreateDTO;
import com.social.post.vo.PostVO;

import java.util.List;
import java.util.Map;

public interface PostService {

    Long createPost(Long userId, PostCreateDTO dto);

    PostVO getPostDetail(Long postId, Long currentUserId);

    Page<PostVO> getPostFeed(Long userId, int pageNum, int pageSize);

    Page<PostVO> getUserPosts(Long userId, int pageNum, int pageSize);

    void deletePost(Long postId, Long userId);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    void collectPost(Long postId, Long userId);

    void uncollectPost(Long postId, Long userId);

    List<PostVO> searchPosts(String keyword, Long currentUserId);

    List<java.util.Map<String, Object>> searchTopics(String keyword);

    Map<String, Object> getPostStats(String date);

    Page<PostVO> getUserCollectedPosts(Long userId, Long currentUserId, int pageNum, int pageSize);

    Page<PostVO> getUserLikedPosts(Long userId, Long currentUserId, int pageNum, int pageSize);
}
