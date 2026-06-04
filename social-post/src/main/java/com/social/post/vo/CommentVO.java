package com.social.post.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO implements Serializable {

    private Long id;

    private Long postId;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String content;

    private Long parentId;

    private Integer likeCount;

    private List<CommentVO> replies;

    private LocalDateTime createTime;
}
