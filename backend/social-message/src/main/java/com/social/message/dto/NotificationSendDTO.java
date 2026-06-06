package com.social.message.dto;

import lombok.Data;

@Data
public class NotificationSendDTO {

    private Long receiverId;

    private Long senderId;

    private String type;

    private String content;

    private Long targetId;
}
