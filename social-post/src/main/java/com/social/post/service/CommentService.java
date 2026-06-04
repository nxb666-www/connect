package com.social.post.service;

import com.social.post.dto.CommentCreateDTO;
import com.social.post.vo.CommentVO;

import java.util.List;

public interface CommentService {

    Long createComment(Long userId, CommentCreateDTO dto);

    List<CommentVO> getPostComments(Long postId);

    void deleteComment(Long commentId, Long userId);
}
