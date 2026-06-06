package com.social.message.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MessageVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    private String senderName;

    private String senderAvatar;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    private String receiverName;

    private String receiverAvatar;

    private String content;

    private Integer type;

    private Integer chatType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;

    private String groupName;

    private Integer isRead;

    private LocalDateTime createTime;
}
