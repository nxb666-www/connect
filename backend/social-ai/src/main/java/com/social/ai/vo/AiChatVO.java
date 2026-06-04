package com.social.ai.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AiChatVO implements Serializable {

    private Long id;

    private String sessionId;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
