package com.social.search.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResultVO implements Serializable {

    private List<UserResult> users;

    private List<PostResult> posts;

    private List<TopicResult> topics;

    @Data
    public static class UserResult implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private String signature;
    }

    @Data
    public static class PostResult implements Serializable {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long postId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;
        private String username;
        private String nickname;
        private String content;
        private String topic;
        private Integer likeCount;
        private Integer commentCount;
    }

    @Data
    public static class TopicResult implements Serializable {
        private String topic;
        private Long postCount;
    }
}
