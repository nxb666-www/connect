package com.social.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.social.common.core.exception.BusinessException;
import com.social.post.dto.CommentCreateDTO;
import com.social.post.entity.Comment;
import com.social.post.entity.Post;
import com.social.post.feign.NotificationFeignClient;
import com.social.post.mapper.CommentMapper;
import com.social.post.mapper.PostMapper;
import com.social.post.service.CommentService;
import com.social.post.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final NotificationFeignClient notificationFeignClient;

    @Override
    @Transactional
    public Long createComment(Long userId, CommentCreateDTO dto) {
        Post post = postMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException("动态不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setDeleted(0);
        commentMapper.insert(comment);

        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Post::getId, dto.getPostId())
                .setSql("comment_count = comment_count + 1");
        postMapper.update(null, updateWrapper);

        if (!post.getUserId().equals(userId)) {
            try {
                Map<String, Object> notif = new HashMap<>();
                notif.put("receiverId", post.getUserId());
                notif.put("senderId", userId);
                notif.put("type", "comment");
                notif.put("content", "评论了你的动态");
                notif.put("targetId", dto.getPostId());
                notificationFeignClient.sendNotification(notif);
            } catch (Exception e) {
                log.warn("发送评论通知失败: {}", e.getMessage());
            }
        }

        return comment.getId();
    }

    @Override
    public List<CommentVO> getPostComments(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .isNull(Comment::getParentId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        List<Comment> topComments = commentMapper.selectList(wrapper);

        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(Comment::getPostId, postId)
                .isNotNull(Comment::getParentId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime);
        List<Comment> replies = commentMapper.selectList(replyWrapper);

        Map<Long, List<Comment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : topComments) {
            CommentVO vo = convertToVO(comment);
            List<Comment> childReplies = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
            vo.setReplies(childReplies.stream().map(this::convertToVO).toList());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人评论");
        }
        commentMapper.deleteById(commentId);

        LambdaUpdateWrapper<Post> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Post::getId, comment.getPostId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)");
        postMapper.update(null, updateWrapper);
    }

    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());
        vo.setReplies(new ArrayList<>());
        return vo;
    }
}
