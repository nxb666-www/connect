package com.social.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.social.common.core.exception.BusinessException;
import com.social.common.core.result.Result;
import com.social.post.dto.PostCreateDTO;
import com.social.post.entity.Collect;
import com.social.post.entity.Comment;
import com.social.post.entity.LikeRecord;
import com.social.post.entity.Post;
import com.social.post.feign.UserFeignClient;
import com.social.post.feign.NotificationFeignClient;
import com.social.post.mapper.CollectMapper;
import com.social.post.mapper.CommentMapper;
import com.social.post.mapper.LikeRecordMapper;
import com.social.post.mapper.PostMapper;
import com.social.post.service.PostService;
import com.social.post.vo.PostVO;
import lombok.RequiredArgsConstructor;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CollectMapper collectMapper;
    private final CommentMapper commentMapper;
    private final ObjectMapper objectMapper;
    private final UserFeignClient userFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final com.social.post.mq.PostMessageProducer postMessageProducer;

    @Override
    @GlobalTransactional(name = "createPost", rollbackFor = Exception.class)
    @Transactional
    public Long createPost(Long userId, PostCreateDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(dto.getContent());
        post.setTopic(dto.getTopic());
        post.setVisibility(dto.getVisibility());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setCollectCount(0);
        post.setStatus(1);
        post.setDeleted(0);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                post.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片格式错误");
            }
        }

        postMapper.insert(post);

        // RocketMQ: 异步通知（无MQ服务器时静默跳过）
        String preview = dto.getContent() != null && dto.getContent().length() > 50
                ? dto.getContent().substring(0, 50) + "..." : dto.getContent();
        postMessageProducer.sendPostCreatedMessage(post.getId(), userId, preview);

        return post.getId();
    }

    @Override
    public PostVO getPostDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        return convertToVO(post, currentUserId);
    }

    @Override
    public Page<PostVO> getPostFeed(Long userId, int pageNum, int pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getVisibility, 1)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(page, wrapper);

        Page<PostVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(postPage.getTotal());
        voPage.setRecords(postPage.getRecords().stream()
                .map(post -> convertToVO(post, userId))
                .toList());
        return voPage;
    }

    @Override
    public Page<PostVO> getUserPosts(Long userId, Long currentUserId, int pageNum, int pageSize) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);
        Page<Post> postPage = postMapper.selectPage(page, wrapper);

        Page<PostVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(postPage.getTotal());
        voPage.setRecords(postPage.getRecords().stream()
                .map(post -> convertToVO(post, currentUserId))
                .toList());
        return voPage;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人动态");
        }
        postMapper.deleteById(postId);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getPostId, postId)
                .eq(LikeRecord::getUserId, userId);
        if (likeRecordMapper.selectCount(wrapper) > 0) {
            return;
        }

        LikeRecord record = new LikeRecord();
        record.setPostId(postId);
        record.setUserId(userId);
        likeRecordMapper.insert(record);

        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Post::getId, postId)
                .setSql("like_count = like_count + 1");
        postMapper.update(null, updateWrapper);

        Post post = postMapper.selectById(postId);
        if (post != null && !post.getUserId().equals(userId)) {
            try {
                Map<String, Object> notif = new HashMap<>();
                notif.put("receiverId", post.getUserId());
                notif.put("senderId", userId);
                notif.put("type", "like");
                notif.put("content", "赞了你的动态");
                notif.put("targetId", postId);
                notificationFeignClient.sendNotification(notif);
            } catch (Exception e) {
                log.warn("发送点赞通知失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<PostVO> searchPosts(String keyword, Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getVisibility, 1)
                .eq(Post::getStatus, 1)
                .and(w -> w.like(Post::getContent, keyword).or().like(Post::getTopic, keyword))
                .orderByDesc(Post::getCreateTime)
                .last("LIMIT 30");
        return postMapper.selectList(wrapper).stream()
                .map(post -> convertToVO(post, currentUserId))
                .toList();
    }

    @Override
    public List<Map<String, Object>> searchTopics(String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getVisibility, 1)
                .eq(Post::getStatus, 1)
                .like(Post::getTopic, keyword)
                .isNotNull(Post::getTopic)
                .select(Post::getTopic)
                .last("LIMIT 100");
        return postMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(Post::getTopic, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("topic", e.getKey());
                    m.put("postCount", e.getValue());
                    return m;
                })
                .limit(10)
                .toList();
    }

    @Override
    public Map<String, Object> getPostStats(String date) {
        Map<String, Object> stats = new java.util.HashMap<>();

        LambdaQueryWrapper<Post> newPostsWrapper = new LambdaQueryWrapper<>();
        newPostsWrapper.apply("DATE(create_time) = {0}", date);
        long newPosts = postMapper.selectCount(newPostsWrapper);
        stats.put("newPosts", newPosts);

        LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.apply("DATE(create_time) = {0}", date);
        long totalLikes = likeRecordMapper.selectCount(likeWrapper);
        stats.put("totalLikes", totalLikes);

        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.apply("DATE(create_time) = {0}", date);
        long totalComments = commentMapper.selectCount(commentWrapper);
        stats.put("totalComments", totalComments);

        return stats;
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getPostId, postId)
                .eq(LikeRecord::getUserId, userId);
        int deleted = likeRecordMapper.delete(wrapper);
        if (deleted > 0) {
            LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Post::getId, postId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)");
            postMapper.update(null, updateWrapper);
        }
    }

    @Override
    @Transactional
    public void collectPost(Long postId, Long userId) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getPostId, postId)
                .eq(Collect::getUserId, userId);
        if (collectMapper.selectCount(wrapper) > 0) {
            return;
        }

        Collect collect = new Collect();
        collect.setPostId(postId);
        collect.setUserId(userId);
        collectMapper.insert(collect);

        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Post::getId, postId)
                .setSql("collect_count = collect_count + 1");
        postMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void uncollectPost(Long postId, Long userId) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getPostId, postId)
                .eq(Collect::getUserId, userId);
        int deleted = collectMapper.delete(wrapper);
        if (deleted > 0) {
            LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Post::getId, postId)
                    .setSql("collect_count = GREATEST(collect_count - 1, 0)");
            postMapper.update(null, updateWrapper);
        }
    }

    @Override
    public Page<PostVO> getUserCollectedPosts(Long userId, Long currentUserId, int pageNum, int pageSize) {
        // 先查出用户收藏的动态ID
        LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(Collect::getUserId, userId)
                .orderByDesc(Collect::getCreateTime);
        List<Collect> collects = collectMapper.selectList(collectWrapper);
        if (collects.isEmpty()) {
            Page<PostVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        List<Long> postIds = collects.stream().map(Collect::getPostId).toList();
        // 分页查询动态
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, postIds.size());
        if (from >= postIds.size()) {
            Page<PostVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(postIds.size());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }
        List<Long> pageIds = postIds.subList(from, to);
        List<Post> posts = postMapper.selectBatchIds(pageIds);

        Page<PostVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(postIds.size());
        voPage.setRecords(posts.stream()
                .map(post -> convertToVO(post, currentUserId))
                .toList());
        return voPage;
    }

    @Override
    public Page<PostVO> getUserLikedPosts(Long userId, Long currentUserId, int pageNum, int pageSize) {
        // 先查出用户点赞的动态ID
        LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(LikeRecord::getUserId, userId)
                .orderByDesc(LikeRecord::getCreateTime);
        List<LikeRecord> likes = likeRecordMapper.selectList(likeWrapper);
        if (likes.isEmpty()) {
            Page<PostVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        List<Long> postIds = likes.stream().map(LikeRecord::getPostId).toList();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, postIds.size());
        if (from >= postIds.size()) {
            Page<PostVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(postIds.size());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }
        List<Long> pageIds = postIds.subList(from, to);
        List<Post> posts = postMapper.selectBatchIds(pageIds);

        Page<PostVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(postIds.size());
        voPage.setRecords(posts.stream()
                .map(post -> convertToVO(post, currentUserId))
                .toList());
        return voPage;
    }

    private PostVO convertToVO(Post post, Long currentUserId) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setContent(post.getContent());
        vo.setTopic(post.getTopic());
        vo.setVisibility(post.getVisibility());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setShareCount(post.getShareCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setCreateTime(post.getCreateTime());

        if (post.getImages() != null) {
            try {
                vo.setImages(objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                vo.setImages(Collections.emptyList());
            }
        }

        try {
            Result<Map<String, Object>> userResult = userFeignClient.getUserInfo(post.getUserId());
            if (userResult != null && userResult.getData() != null) {
                Map<String, Object> userData = userResult.getData();
                vo.setUsername((String) userData.get("username"));
                vo.setNickname((String) userData.get("nickname"));
                vo.setAvatar((String) userData.get("avatar"));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: userId={}, error={}", post.getUserId(), e.getMessage());
        }

        if (currentUserId != null) {
            LambdaQueryWrapper<LikeRecord> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(LikeRecord::getPostId, post.getId())
                    .eq(LikeRecord::getUserId, currentUserId);
            vo.setIsLiked(likeRecordMapper.selectCount(likeWrapper) > 0);

            LambdaQueryWrapper<Collect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(Collect::getPostId, post.getId())
                    .eq(Collect::getUserId, currentUserId);
            vo.setIsCollected(collectMapper.selectCount(collectWrapper) > 0);
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
        }

        return vo;
    }
}
