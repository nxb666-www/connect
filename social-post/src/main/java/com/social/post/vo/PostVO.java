package com.social.post.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO implements Serializable {

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String content;

    private List<String> images;

    private String topic;

    private Integer visibility;

    private Integer likeCount;

    private Integer commentCount;

    private Integer shareCount;

    private Integer collectCount;

    private Boolean isLiked;

    private Boolean isCollected;

    private LocalDateTime createTime;
}
