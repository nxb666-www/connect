package com.social.message.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MessageVO implements Serializable {

    private Long id;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private Long receiverId;

    private String receiverName;

    private String receiverAvatar;

    private String content;

    private Integer type;

    private Integer isRead;

    private LocalDateTime createTime;
}
