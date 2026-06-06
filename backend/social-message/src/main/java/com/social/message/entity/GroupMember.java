package com.social.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("group_member")
public class GroupMember implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    private String nickname;

    private Integer role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
