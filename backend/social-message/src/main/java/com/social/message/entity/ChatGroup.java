package com.social.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("chat_group")
public class ChatGroup implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private Long ownerId;

    private Integer memberCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
