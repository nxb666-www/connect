package com.social.message.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String type;

    private String content;

    private Long targetId;

    private Integer isRead;

    private LocalDateTime createTime;
}
